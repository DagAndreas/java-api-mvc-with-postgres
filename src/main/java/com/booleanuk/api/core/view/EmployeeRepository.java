package com.booleanuk.api.core.view;

import com.booleanuk.api.core.model.Employee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

	@Getter
	@Setter
	@AllArgsConstructor
	@ToString
	public static class Salary {
		private String grade;
		private int minSalary;
		private int maxSalary;
	}

	@RestController
	@RequestMapping("/salaries")
	public static class SalaryController {
		SalaryRepository repo;

		public SalaryController() throws SQLException {
			System.out.println("Created new salary repo");
			this.repo = new SalaryRepository();
		}

		//get all
		@GetMapping
		public List<Salary> getAll() throws SQLException  {
			System.out.println();
			return repo.getAll();
		}

		// get by id
		@GetMapping("/{id}")
		public Salary getOne(@PathVariable(name = "id") String id) throws SQLException {
			Salary salary = repo.get(id);
			if (salary == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
			}
			return salary;
		}


		// add new
		@PostMapping
		@ResponseStatus(HttpStatus.CREATED)
		public Salary create(@RequestBody Salary emp) throws SQLException {
			Salary salary = repo.add(emp);
			if (salary == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to create the specified Customer");
			}
			return salary;
		}

		// edit by id
		@PutMapping("/{grade}")
		@ResponseStatus(HttpStatus.CREATED)
		public Salary update(@PathVariable String grade, @RequestBody Salary salary) throws SQLException {
			Salary toBeUpdated = repo.get(grade);
			if (toBeUpdated == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
			}
			return repo.update(grade, salary);
		}


		// delete by id
		@DeleteMapping("/{id}")
		public Salary delete(@PathVariable (name = "id") String id) throws SQLException {
			Salary toBeDeleted = repo.get(id);
			if (toBeDeleted == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
			}
			return repo.delete(id);
		}
	}

	public static class SalaryRepository {
		public DataSource datasource;
		public String dbUser;
		public String dbURL;
		public String dbPassword;
		public String dbDatabase;
		public Connection connection;

		public SalaryRepository() throws SQLException {
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

		public List<Salary> getAll() throws SQLException  {
			List<Salary> everyone = new ArrayList<>();
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM Salaries");
			ResultSet results = statement.executeQuery();

			while (results.next()) {
				Salary newSalary = new Salary(results.getString("grade"), results.getInt("minSalary"), results.getInt("maxSalary"));
				everyone.add(newSalary);
			}
			return everyone;
		}


		public Salary get(String grade) throws SQLException {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM Salaries WHERE grade=?");
			statement.setString(1, grade);

			ResultSet results = statement.executeQuery();
			if (!results.next()){
				throw new SQLException("id not found");
			}
			return new Salary(results.getString("grade"), results.getInt("minSalary"), results.getInt("maxSalary"));
		}


		public Salary add(Salary salary) throws SQLException {
			String SQL = "INSERT INTO Salaries(grade, minSalary, maxSalary) VALUES (?, ?, ?);";
			PreparedStatement statement = this.connection.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, salary.getGrade());
			statement.setInt(2, salary.getMinSalary());
			statement.setInt(3, salary.getMaxSalary());
			int rowsAffected = statement.executeUpdate();
			String newGrade = "";
			if (rowsAffected > 0) {
				try (ResultSet rs = statement.getGeneratedKeys()) {
					if (rs.next()) {
						newGrade = rs.getString(1);
					}
				} catch (Exception e) {
					System.out.println("Oops: " + e);
				}
				salary.setGrade(newGrade);
			} else {
				salary = null;
			}
			return salary;
		}

			public Salary update(String id, Salary salary) throws SQLException {
				String SQL = "UPDATE Salaries " +
						"SET grade = ? , " +
						"minSalary = ? , " +
						"maxSalary = ? ";
		//				" WHERE grade = ? ;";
				PreparedStatement statement = this.connection.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
				statement.setString(1, salary.getGrade());
				statement.setInt(2, salary.getMinSalary());
				statement.setInt(3, salary.getMaxSalary());
		//		statement.setString(4, id);
				System.out.println(statement.toString());
				int rowsAffected = statement.executeUpdate();
				Salary updatedSalary = null;
				if (rowsAffected > 0) {
					updatedSalary = get(id);
				}
				return updatedSalary;
			}

		public Salary delete(String grade) throws SQLException {
			String SQL = "DELETE FROM Salarys WHERE id = ?";
			PreparedStatement statement = this.connection.prepareStatement(SQL);


			Salary deletedSalary = null;
			deletedSalary = this.get(grade);

			statement.setString(1, grade);
			int rowsAffected = statement.executeUpdate();
			if (rowsAffected == 0) {
				deletedSalary = null;
			}
			return deletedSalary;
		}
	}
}
