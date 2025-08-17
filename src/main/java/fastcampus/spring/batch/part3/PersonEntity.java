package fastcampus.spring.batch.part3;

import lombok.Getter;

@Getter
public class PersonEntity {
  private final int id;
  private final String name;
  private final String age;
  private final String address;

  public PersonEntity(int id, String name, String age, String address) {
    this.id = id;
    this.name = name;
    this.age = age;
    this.address = address;
  }
}
