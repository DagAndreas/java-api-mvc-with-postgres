package com.booleanuk.api.core;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EmployeeRepository {
	public DataSource datasource;
	public String dbUser;
	public String dbURL;
	public String dbPassword;
	public String dbDatabase;
	public Connection connection;

	public EmployeeRepository() throws SQLException {
		this.getDatabaseCredentials();
		this.datasource = this.createDataSource();
		this.connection = this.datasource.getConnection();
	}

	public void getDatabaseCredentials() {
		try (InputStream input = new FileInputStream("src/main/resources/config.properties")) {
			Properties prop = new Properties();
			prop.load(input);
			this.dbUser = prop.getProperty("db.user");
			this.dbURL = prop.getProperty("db.url");
			this.dbPassword = prop.getProperty("db.password");
			this.dbDatabase = prop.getProperty("db.database");

			System.out.println("" + dbDatabase + ", " + dbURL + ", " + dbUser + ", " + dbPassword);

		} catch(Exception e) {
			System.out.println("Failed when fetching credentials from config.properties: " + e);
		}
	}

	public DataSource createDataSource() {
		final String url =
				"jdbc:postgresql://" + this.dbURL
						+ ":5432/" + this.dbDatabase
						+ "?user=" + this.dbUser
						+"&password=" + this.dbPassword;
		final PGSimpleDataSource dataSource = new PGSimpleDataSource();
		System.out.println(url);
		dataSource.setUrl(url);
		return dataSource;
	}

	public List<Employee> getAll() throws SQLException  {
		List<Employee> everyone = new ArrayList<>();
		PreparedStatement statement = connection.prepareStatement("SELECT * FROM Employees");

		ResultSet results = statement.executeQuery();

		while (results.next()) {
			Employee newEmployee = new Employee(results.getLong("id"), results.getString("name"), results.getString("jobName"), results.getString("salaryGrade"), results.getString("department"));
			everyone.add(newEmployee);
		}
		return everyone;
	}

	public Employee get(long id) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("SELECT * FROM Employees WHERE id=?");
		statement.setLong(1, id);

		ResultSet results = statement.executeQuery();
		if (!results.next()){
			throw new SQLException("id not found");
		}
		return new Employee(results.getLong("id"), results.getString("name"), results.getString("jobName"), results.getString("salaryGrade"), results.getString("department"));
	}


	public Employee add(Employee employee) throws SQLException {
		String SQL = "INSERT INTO Employees(name, jobName, salaryGrade, department) VALUES(?, ?, ?, ?)";
		PreparedStatement statement = this.connection.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, employee.getName());
		statement.setString(2, employee.getJobName());
		statement.setString(3, employee.getSalaryGrade());
		statement.setString(4, employee.getDepartment());
		int rowsAffected = statement.executeUpdate();
		long newId = 0;
		if (rowsAffected > 0) {
			try (ResultSet rs = statement.getGeneratedKeys()) {
				if (rs.next()) {
					newId = rs.getLong(1);
				}
			} catch (Exception e) {
				System.out.println("Oops: " + e);
			}
			employee.setId(newId);
		} else {
			employee = null;
		}
		return employee;
	}

	public Employee update(long id, Employee employee) throws SQLException {
		String SQL = "UPDATE Employees " +
				"SET name = ? ," +
				"jobName = ? ," +
				"salaryGrade = ? ," +
				"department = ? " +
				"WHERE id = ? ";
		PreparedStatement statement = connection.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, employee.getName());
		statement.setString(2, employee.getJobName());
		statement.setString(3, employee.getSalaryGrade());
		statement.setString(4, employee.getDepartment());
		statement.setLong(5, id);
		int rowsAffected = statement.executeUpdate();
		Employee updatedEmployee = null;
		if (rowsAffected > 0) {
			updatedEmployee = get(id);
		}
		return updatedEmployee;
	}

	public Employee delete(long id) throws SQLException {
		String SQL = "DELETE FROM Employees WHERE id = ?";
		PreparedStatement statement = this.connection.prepareStatement(SQL);


		Employee deletedEmployee = null;
		deletedEmployee = this.get(id);

		statement.setLong(1, id);
		int rowsAffected = statement.executeUpdate();
		if (rowsAffected == 0) {
			deletedEmployee = null;
		}
		return deletedEmployee;
	}

}
