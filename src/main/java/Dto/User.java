package Dto;

import java.sql.Timestamp;
import java.util.Objects;

public class User {
  public String id;
  public String name;
  public String email;
  public String genre;
  public Timestamp registerDate;

  public User() {}

  public User(String id, String name, String email, String genre, String registerDate) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.genre = genre;
    this.registerDate = Timestamp.valueOf(registerDate);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("User{");
    sb.append("id=").append(id).append('\'');
    sb.append(", name=").append(name).append('\'');
    sb.append(", email=").append(email).append('\'');
    sb.append(", genre=").append(genre).append('\'');
    sb.append(", registerDate=").append(String.valueOf(registerDate)).append('\'');
    sb.append('}');
    return sb.toString();
  }

  public int hashCode() {
    return Objects.hash(id, name, email, genre, registerDate);
  }
}