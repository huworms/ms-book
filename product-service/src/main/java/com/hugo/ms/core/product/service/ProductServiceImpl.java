package com.hugo.ms.core.product.service;

import static java.util.logging.Level.FINE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.hugo.api.core.exception.InvalidInputException;
import com.hugo.api.core.exception.NotFoundException;
import com.hugo.api.core.product.Product;
import com.hugo.api.core.product.ProductService;
import com.hugo.ms.core.product.persistence.ProductEntity;
import com.hugo.ms.core.product.persistence.ProductRepository;
import com.hugo.util.ServiceUtil;
import com.mongodb.DuplicateKeyException;

import reactor.core.publisher.Mono;

@RestController
public class ProductServiceImpl implements ProductService{

	private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);
	
	private final ServiceUtil serviceUtil;
	private final ProductRepository repository;
	private final ProductMapper mapper;
	
	
	@Autowired
	public ProductServiceImpl(
			ProductRepository repository,
			ProductMapper mapper,
			ServiceUtil serviceUtil) {
		this.serviceUtil=serviceUtil;
		this.mapper=mapper;
		this.repository=repository;
		
	}
	
	@Override
	public Mono<Product> getProduct(int productId) {
		 LOG.debug("/product return the found product for productId={}", productId);
		 if (productId < 1) {
	      throw new InvalidInputException("Invalid productId: " + productId);
	     }
		 LOG.info("will get product info for id={}",productId);
		 
		 return repository.findByProductId(productId)
		 	.switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: " 
	 						+ productId)))
		 	.log(LOG.getName(), FINE)
		 	.map(e-> mapper.entityToApi(e))
		 	.map(e-> setServiceAddress(e));
		 	
		 	
	}

	@Override
	public Mono<Product> createProduct(Product body) {
		
		if (body.getProductId() < 1) {
	      throw new InvalidInputException("Invalid productId: " + body.getProductId());
	    }
		
		ProductEntity entity= mapper.apiToEntity(body);
		Mono<Product> newEntity= this.repository.save(entity)
				.log(LOG.getName(), FINE)
				.onErrorMap(
					DuplicateKeyException.class,
					ex-> new InvalidInputException("Duplicate key, Product Id: " 
						+ body.getProductId())
					)
				.map(e->mapper.entityToApi(e));
		
		return newEntity;
		
	}

	@Override
	public Mono<Void> deleteProduct(int productId) {
		if (productId < 1) {
	      throw new InvalidInputException("Invalid productId: " + productId);
	    }
		
		LOG.debug("deleteProduct: tries to delete an entity with  with productId: {}", productId);
		return repository.findByProductId(productId)
			.log(LOG.getName(), FINE)
			.map(e-> repository.delete(e))
			.flatMap(e->e);
		
	}
	
	private Product setServiceAddress(Product e) {
	    e.setServiceAddress(serviceUtil.getServiceAddress());
	    return e;
	}

}
