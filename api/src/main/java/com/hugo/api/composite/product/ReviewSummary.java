package com.hugo.api.composite.product;

public class ReviewSummary {
	private final int reviewId;
	private final String author;
	private final String subject;
	private final String content;
	
	public ReviewSummary(int reviewId, String author, 
			String subject, String content) {
		super();
		this.reviewId = reviewId;
		this.author = author;
		this.subject = subject;
		this.content=content;
	}

	public String getContent() {
		return content;
	}

	public int getReviewId() {
		return reviewId;
	}

	public String getAuthor() {
		return author;
	}

	public String getSubject() {
		return subject;
	}
}
