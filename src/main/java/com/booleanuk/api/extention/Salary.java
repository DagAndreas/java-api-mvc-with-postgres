package com.booleanuk.api.extention;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class Salary {
	private String grade;
	private int minSalary;
	private int maxSalary;
}
