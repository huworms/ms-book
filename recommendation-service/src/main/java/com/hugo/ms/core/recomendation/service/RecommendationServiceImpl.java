package com.hugo.ms.core.recomendation.service;

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
	public Recommendation createRecommendation(Recommendation body) {
		try {
			LOG.debug("productID: "+body.getProductId()+", recommendationID: "+body.getRecommendationId());
			RecommendationEntity entity=mapper.apiToEntity(body);
			LOG.debug("Entity productID : "+entity.getProductId()+",Entity recommendationID: "+entity.getRecommendationId());
			RecommendationEntity newEntity=repository.save(entity);
			
			LOG.debug("createRecommendation: created a recommendation "
					+ "entity: {}/{}",body.getProductId(),body.getRecommendationId());
			return mapper.entityToApi(newEntity);
		}catch(DuplicateKeyException dke) {
			throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId());
		}	
	}
    
    @Override
    public List<Recommendation> getRecommendations(int productId) {
    	LOG.info("id_product: "+productId);

      if (productId < 1) {
        throw new InvalidInputException("Invalid productId: " + productId);
      }

      List<RecommendationEntity> entityList=repository.findByProductId(productId);
      List<Recommendation> list= mapper.entityListToApiList(entityList);
      list.forEach(e->e.setServiceAddress(serviceUtil.getServiceAddress()));
      
      LOG.debug("getRecommendations: response size(): {}", list.size());
      
      return list;
    }


	@Override
	public void deleteRecommendations(int productId) {
		LOG.debug("deleteRecommendations:  tries to delete "
				+ "recommendations for the product with productId: {}", productId);
		repository.deleteAll(repository.findByProductId(productId));
		
	}
}
