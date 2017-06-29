# Configuration variables
from typing import List, Set

import requests

schema_version = '3.2'
allowed_extensions = {'http://health-lifesci.schema.org', 'http://bib.schema.org', 'http://auto.schema.org'}
allowed_roots = {  # Ordered according to http://schema.org/docs/full.html
    'http://schema.org/CreativeWork',
    'http://schema.org/Brand',
    'http://schema.org/BedDetails',
    'http://schema.org/BroadcastChannel',
    'http://schema.org/BroadcastFrequencySpecification',
    'http://schema.org/BusTrip',
    'http://schema.org/ComputerLanguage',
    'http://schema.org/Flight',
    'http://schema.org/GameServer',
    'http://schema.org/JobPosting',
    'http://schema.org/Language',
    'http://schema.org/MenuItem',
    'http://schema.org/Offer',
    'http://schema.org/Order',
    'http://schema.org/OrderItem',
    'http://schema.org/ParcelDelivery',
    'http://schema.org/Permit',
    'http://schema.org/ProgramMembership',
    'http://schema.org/Reservation',
    'http://schema.org/Seat',
    'http://schema.org/Service',
    'http://schema.org/ServiceChannel',
    'http://schema.org/ContactPoint',
    'http://schema.org/GeoCoordinates',
    'http://schema.org/GeoShape',
    'http://schema.org/TrainTrip',
    'http://schema.org/Organization',
    'http://schema.org/Person',
    'http://schema.org/Place',
    'http://schema.org/Product'
}
datatype_map = {
    'http://schema.org/Boolean': 'xsd:boolean',
    'http://schema.org/Date': 'xsd:date',
    'http://schema.org/DateTime': 'xsd:dateTime',
    'http://schema.org/Number': 'xsd:decimal',
    'http://schema.org/Float': 'xsd:double',
    'http://schema.org/Integer': 'xsd:integer',
    'http://schema.org/Text': 'xsd:string',
    'http://schema.org/URL': 'xsd:anyURI',
    'http://schema.org/Time': 'xsd:time',
    'http://schema.org/Duration': 'xsd:duration'  # is not a dataype in schema.org
}
base_type = 'owl:NamedIndividual'


def _to_set(element):
    if isinstance(element, List) or isinstance(element, Set):
        return set(element)
    else:
        return {element}


def _to_id_set(element):
    if not element:
        return set()
    elif isinstance(element, List) or isinstance(element, Set):
        return set(e['@id'] for e in element)
    else:
        return {element['@id']}


schema = requests.get('http://schema.org/version/{}/all-layers.jsonld'.format(schema_version)).json()
skipped_parts = set()
ignored_types = set()
properties = {}
classes = {}
for graph in schema['@graph']:
    for element in graph['@graph']:
        # Required elements
        if '@id' not in element or '@type' not in element:
            print('Element without @type or @id: {}'.format(element))
            continue
        # We ignore deprecated elements
        if 'http://schema.org/supersededBy' in element:
            continue
        # We filter extensions
        if 'http://schema.org/isPartOf' in element:
            if element['http://schema.org/isPartOf']['@id'] not in allowed_extensions:
                skipped_parts.add(element['http://schema.org/isPartOf']['@id'])
                continue

        # We load interesting elements
        types = _to_set(element['@type'])
        if 'rdf:Property' in types:
            properties[element['@id']] = {
                '@id': element['@id'],
                '@type': types,
                'http://schema.org/domainIncludes': _to_id_set(element.get('http://schema.org/domainIncludes', ())),
                'http://schema.org/rangeIncludes': _to_id_set(element.get('http://schema.org/rangeIncludes', ()))
            }
        elif 'rdfs:Class' in element['@type']:
            classes[element['@id']] = {
                '@id': element['@id'],
                '@type': types,
                'rdfs:subClassOf': _to_id_set(element.get('rdfs:subClassOf', ()))
            }
        else:
            ignored_types.update(types)

# We see which classes we keep
allowed_classes = set(allowed_roots)
changes = True
while changes:
    changes = False
    for class_ in classes.values():
        if class_['@id'] not in allowed_classes and class_['rdfs:subClassOf'] & allowed_classes:
            allowed_classes.add(class_['@id'])
            changes = True

# We output them

# We work on properties
for property in properties.values():
    filtered_range = property['http://schema.org/domainIncludes'] & allowed_classes
    if not filtered_range:
        continue  # TODO: we want to add some properties anyway
    datatype_range = set()
    object_range = set()
    for range in property['http://schema.org/rangeIncludes']:
        if range in datatype_map:
            datatype_range.add(datatype_map[range])
        elif range in classes:
            if range == 'http://schema.org/Thing':
                object_range.add(base_type)
            elif range in allowed_classes:
                object_range.add(range)
            else:
                print('{}: {}'.format(property['@id'], range))
        else:
            print('Unknown range: {}'.format(range))
    if datatype_range and object_range:
        print('Datatype and object range: {}'.format(property['@id']))

if skipped_parts:
    print('Some extension have been ignored: {}'.format(skipped_parts))
if ignored_types:
    print('Elements of these types have been ingored: {}'.format(ignored_types))

# {'@id': 2176, '@type': 2171, 'http://schema.org/domainIncludes': 1154, 'http://schema.org/rangeIncludes': 1148, 'rdfs:comment': 2144, 'rdfs:label': 2146, 'http://schema.org/supersededBy': 82, 'rdfs:subClassOf': 757, 'http://purl.org/dc/terms/source': 404, 'http://www.w3.org/2002/07/owl#equivalentProperty': 13, 'rdfs:subPropertyOf': 93, 'http://schema.org/category': 127, 'http://schema.org/inverseOf': 32, http://schema.org/isPartOf})
