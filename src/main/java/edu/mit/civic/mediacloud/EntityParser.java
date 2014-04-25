package edu.mit.civic.mediacloud;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bericotech.clavin.resolver.LocationResolver;
import com.bericotech.clavin.resolver.ResolvedLocation;

import edu.mit.civic.mediacloud.extractor.ExtractedEntities;
import edu.mit.civic.mediacloud.extractor.StanfordThreeClassExtractor;
import edu.mit.civic.mediacloud.orgs.OrganizationResolver;
import edu.mit.civic.mediacloud.orgs.ResolvedOrganization;
import edu.mit.civic.mediacloud.people.PersonResolver;
import edu.mit.civic.mediacloud.people.ResolvedPerson;

/**
 * Patterned after com.bericotech.clavin.GeoParser
 * @author rahulb
 *
 */
public class EntityParser {

    private static final Logger logger = LoggerFactory.getLogger(EntityParser.class);
    
    // entity extractor to find location names in text
    private StanfordThreeClassExtractor extractor;
    
    private LocationResolver locationResolver;
    private PersonResolver personResolver;
    private OrganizationResolver organizationResolver;
    
    // switch controlling use of fuzzy matching
    private final boolean fuzzy;

    public EntityParser(StanfordThreeClassExtractor extractor, LocationResolver resolver,
            boolean fuzzy) {
        this.extractor = extractor;
        this.locationResolver = resolver;
        this.personResolver = new PersonResolver();
        this.organizationResolver = new OrganizationResolver();
        this.fuzzy = fuzzy;
    }

    public ExtractedEntities extractAndResolve(String inputText) throws Exception {
        logger.trace("input: {}", inputText);
        ExtractedEntities extractedEntities = extractor.extractEntities(inputText);
        logger.trace("extracted: {}", extractedEntities.getLocations());
        return resolve(extractedEntities);
    }
        
    public ExtractedEntities resolve(ExtractedEntities entities) throws Exception{
        
        // resolve the extracted location names against a
        // gazetteer to produce geographic entities representing the
        // locations mentioned in the original text
        List<ResolvedLocation> resolvedLocations = locationResolver.resolveLocations(entities.getLocations(), fuzzy);
        entities.setResolvedLocations( resolvedLocations );
        logger.trace("resolvedLocations: {}", resolvedLocations);
        
        // Disambiguate people
        List<ResolvedPerson> resolvedPeople = personResolver.resolve(entities.getPeople());
        entities.setResolvedPeople( resolvedPeople );
        logger.trace("resolvedPeople: {}", resolvedLocations);

        // Disambiguate organizations
        List<ResolvedOrganization> resolvedOrganizations = organizationResolver.resolve(entities.getOrganizations());
        entities.setResolvedOrganizations( resolvedOrganizations );
        logger.trace("resolvedOrganizations: {}", resolvedOrganizations);

        return entities;
        
    }
    
}
