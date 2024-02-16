package com.hugo.ms.core.recomendation.service;

import static java.util.logging.Level.FINE;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.hugo.api.core.exception.InvalidInputException;
import com.hugo.api.core.recommendation.Recommendation;
import com.hugo.api.core.recommendation.RecommendationService;
import com.hugo.ms.core.recomendation.persistence.RecommendationEntity;
import com.hugo.ms.core.recomendation.persistence.RecommendationRepository;
import com.hugo.util.ServiceUtil;
import com.mongodb.DuplicateKeyException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class RecommendationServiceImpl implements RecommendationService{
	
	private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private final RecommendationRepository repository;
    private final RecommendationMapper mapper;
    

    @Autowired
	    public RecommendationServiceImpl(
	    		RecommendationRepository repository,
	    		RecommendationMapper mapper,
	    		ServiceUtil serviceUtil) {
    	this.repository=repository;
    	this.mapper=mapper;
	    this.serviceUtil = serviceUtil;
    }
    
    @Override
	public Mono<Recommendation> createRecommendation(Recommendation body) {
    	if (body.getProductId() < 1) {
	      throw new InvalidInputException("Invalid productId: " + body.getProductId());
	    }
    	
    	LOG.debug("productID: "+body.getProductId()+", recommendationID: "+body.getRecommendationId());
		RecommendationEntity entity=mapper.apiToEntity(body);
		LOG.debug("Entity productID : "+entity.getProductId()+",Entity recommendationID: "+entity.getRecommendationId());
    	Mono<Recommendation> newEntity=repository.save(entity)
    			.log(LOG.getName(), FINE)
    			.onErrorMap(DuplicateKeyException.class,
    					ex-> new InvalidInputException("Duplicate key, Product Id: " + 
    								body.getProductId() + 
    								", Recommendation Id:" + 
    								body.getRecommendationId()))
				.map(e->mapper.entityToApi(e));
    	return newEntity;
			
	}
    
    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
     
      LOG.info("id_product: "+productId);

      if (productId < 1) {
        throw new InvalidInputException("Invalid productId: " + productId);
      }

      return repository.findByProductId(productId)
    		  .log(LOG.getName(), FINE)
    		  .map(e-> mapper.entityToApi(e))
    		  .map(e-> setServiceAddress(e));
      
    }


	@Override
	public Mono<Void> deleteRecommendations(int productId) {
		if (productId < 1) {
	      throw new InvalidInputException("Invalid productId: " + productId);
	    }
		LOG.debug("deleteRecommendations:  tries to delete "
				+ "recommendations for the product with productId: {}", productId);
		return repository.deleteAll(repository.findByProductId(productId));
		
	}
	
	private Recommendation setServiceAddress(Recommendation e) {
	    e.setServiceAddress(serviceUtil.getServiceAddress());
	    return e;
	}
	
	
}
