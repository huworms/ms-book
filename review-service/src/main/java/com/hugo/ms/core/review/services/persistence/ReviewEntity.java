package com.hugo.ms.core.review.services.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name="reviews", 
indexes= { @Index(name="review_unique_idx",
		unique=true, columnList="productId, reviewId") })
public class ReviewEntity {

	@Id @GeneratedValue
	private int id;
	
	@Version
	private int version;
	
	private int productId;
	private int reviewId;
	private String author;
	private String subject;
	private String content;
	
	public ReviewEntity() {}

	public ReviewEntity(int productId, int reviewId, String author, String subject, String content) {
		super();
		this.productId = productId;
		this.reviewId = reviewId;
		this.author = author;
		this.subject = subject;
		this.content = content;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public int getReviewId() {
		return reviewId;
	}

	public void setReviewId(int reviewId) {
		this.reviewId = reviewId;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	
}