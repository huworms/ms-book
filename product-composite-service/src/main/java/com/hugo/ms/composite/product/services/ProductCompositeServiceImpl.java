package com.hugo.ms.composite.product.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.hugo.api.composite.product.ProductAggregate;
import com.hugo.api.composite.product.ProductCompositeService;
import com.hugo.api.composite.product.RecommendationSummary;
import com.hugo.api.composite.product.ReviewSummary;
import com.hugo.api.composite.product.ServiceAddresses;
import com.hugo.api.core.product.Product;
import com.hugo.api.core.recommendation.Recommendation;
import com.hugo.api.core.review.Review;
import com.hugo.util.ServiceUtil;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

	private final ServiceUtil serviceUtil;
	private ProductCompositeIntegration integration;
	
	  
	@Autowired
	public ProductCompositeServiceImpl(ServiceUtil serviceUtil, 
	ProductCompositeIntegration integration) {
	 this.serviceUtil = serviceUtil;
	 this.integration = integration;
	}
	
	@Override
	public ProductAggregate getProduct(int productId) {
		Product product = integration.getProduct(productId);
		List<Recommendation> recommendations = 
		integration.getRecommendations(productId);
		List<Review> reviews = integration.getReviews(productId);
		 
		return createProductAggregate(product, recommendations,
		reviews, serviceUtil.getServiceAddress());
	}
	
	private ProductAggregate createProductAggregate(
	    Product product,
	    List<Recommendation> recommendations,
	    List<Review> reviews,
	    String serviceAddress) {

	    // 1. Setup product info
	    int productId = product.getProductId();
	    String name = product.getName();
	    int weight = product.getWeight();

	    // 2. Copy summary recommendation info, if available
	    List<RecommendationSummary> recommendationSummaries =
	      (recommendations == null) ? null : recommendations.stream()
	        .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate()))
	        .collect(Collectors.toList());

	    // 3. Copy summary review info, if available
	    List<ReviewSummary> reviewSummaries = 
	      (reviews == null) ? null : reviews.stream()
	        .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject()))
	        .collect(Collectors.toList());

	    // 4. Create info regarding the involved microservices addresses
	    String productAddress = product.getServiceAddress();
	    String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
	    String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
	    ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

	    return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
	  }
	

}