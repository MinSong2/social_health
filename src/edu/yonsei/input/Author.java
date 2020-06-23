package edu.yonsei.input;

public class Author {
	String lastName = null;
	String firstName = null;
	String initial = null;
	String affiliation = null;
	String email = null;
	
	public Author()
	{
		affiliation = "";
	}
	
	public Author(String lastName, String firstName, String initial, String affiliation) {
		this.lastName = lastName;
		this.firstName = firstName;
		this.initial = initial;
		this.affiliation = affiliation;
	}
	
	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}
	
	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}
	
	public void setInitial(String initial)
	{
		this.initial = initial;
	}
	
	public void setAffiliation(String affiliation)
	{
		this.affiliation = affiliation;
	}
	
	public String getLastName()
	{
		return lastName;
	}
	
	public String getFirstName()
	{
		return firstName;
	}
	
	public String initial()
	{
		return initial;
	}
	
	public String affiliation()
	{
		return affiliation;
	}
	
	public void setEmail(String email)
	{
		this.email = email;
	}
	
	public String getEmail()
	{
		return email;
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("First Name " + firstName + "\n");
		sb.append("Last Name " + lastName + "\n");
		sb.append("Affiliation " + affiliation + "\n");
		sb.append("Email " + email + "\n");
		
		return sb.toString();
	}
}