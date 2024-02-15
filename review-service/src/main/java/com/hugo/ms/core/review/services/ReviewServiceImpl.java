package com.hugo.ms.core.review.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;

import com.hugo.api.core.exception.InvalidInputException;
import com.hugo.api.core.review.Review;
import com.hugo.api.core.review.ReviewService;
import com.hugo.ms.core.review.services.persistence.ReviewEntity;
import com.hugo.ms.core.review.services.persistence.ReviewRepository;
import com.hugo.util.ServiceUtil;

@RestController
public class ReviewServiceImpl implements ReviewService{

	  private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

	  private final ServiceUtil serviceUtil;
	  private final ReviewRepository repository;
	  private final ReviewMapper mapper;
	  
	  	
	  @Autowired
	  public ReviewServiceImpl(
			  ReviewRepository repository,
			  ReviewMapper mapper,
			  ServiceUtil serviceUtil) {
	    
		  this.repository=repository;
		  this.mapper=mapper;
		  this.serviceUtil = serviceUtil;
	  }
	  
	  @Override
	  public Review createReview(Review body) {
		  try {
			  ReviewEntity entity=mapper.apiToEntity(body);
			  ReviewEntity newEntity=repository.save(entity);
			  LOG.debug("createReview: created a review entity: {}/{}",
					  body.getProductId(), body.getReviewId());
			  
			  return mapper.entityToApi(newEntity);
		  }catch(DataIntegrityViolationException dive) {
			  throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + 
					  ", Review Id:" + body.getReviewId());
			  
		  }
	  }
	  
	  @Override
	  public List<Review> getReviews(int productId) {

	    if (productId < 1) {
	      throw new InvalidInputException("Invalid productId: " + productId);
	    }
	
	    List<ReviewEntity> entityList=repository.findByProductId(productId);
	    List<Review> list=mapper.entityListToApiList(entityList);
	    list.forEach(e->e.setServiceAddress(serviceUtil.getServiceAddress()));
	    
	    LOG.debug("getReviews: response size: {}", list.size());
	
	    return list;
	 }

	

	@Override
	public void deleteReviews(int productId) {
		LOG.debug("deleteRevies: tries to delete revies for the "
				+ "product with productId: {}",productId);
		repository.deleteAll(repository.findByProductId(productId));
		
	}
}
