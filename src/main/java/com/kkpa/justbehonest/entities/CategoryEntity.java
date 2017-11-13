package com.kkpa.justbehonest.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="CATEGORY")
public class CategoryEntity {
	
	@Id
	@Column(name="ID_CATEGORY")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="category_secuence")
	@SequenceGenerator (name = "category_secuence" , sequenceName="SEQ_CATEGORY_ID")
	private Long idCategory;
	
	@Column(name="NAME",length=10, unique=true , nullable=false)
	private String name;
	
	@Column(name="TYPE",length=1 , nullable=false)
	private String type;

	public Long getIdCategory() {
		return idCategory;
	}

	public void setIdCategory(Long idCategory) {
		this.idCategory = idCategory;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
