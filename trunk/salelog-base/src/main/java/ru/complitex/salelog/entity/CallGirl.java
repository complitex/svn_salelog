package ru.complitex.salelog.entity;

/**
 * @author Pavel Sknar
 */
public class CallGirl extends DictionaryObject {

    private String code;
    private Person person = new Person();

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
