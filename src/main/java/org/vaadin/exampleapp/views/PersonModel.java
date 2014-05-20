package org.vaadin.exampleapp.views;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import org.vaadin.exampleapp.data.Person;
import org.vaadin.exampleapp.data.Skill;

public class PersonModel {

	private final PropertyChangeSupport notifier = new PropertyChangeSupport(
			this);

	private Person selectedPerson;

	private List<Person> persons;

	private List<Skill> availableSkills;

	public void addPropertyChangeListener(PropertyChangeListener arg0) {
		notifier.addPropertyChangeListener(arg0);
	}

	public void addPropertyChangeListener(String arg0,
			PropertyChangeListener arg1) {
		notifier.addPropertyChangeListener(arg0, arg1);
	}

	public void removePropertyChangeListener(PropertyChangeListener arg0) {
		notifier.removePropertyChangeListener(arg0);
	}

	public void removePropertyChangeListener(String arg0,
			PropertyChangeListener arg1) {
		notifier.removePropertyChangeListener(arg0, arg1);
	}

	public Person getSelectedPerson() {
		return selectedPerson;
	}

	public void setSelectedPerson(Person selectedPerson) {
		Person oldValue = this.selectedPerson;
		this.selectedPerson = selectedPerson;
		notifier.firePropertyChange("selectedPerson", oldValue, selectedPerson);
	}

	public List<Person> getPersons() {
		return persons;
	}

	public void setPersons(List<Person> persons) {
		List<Person> oldValue = this.persons;
		this.persons = persons;
		notifier.firePropertyChange("persons", oldValue, persons);
	}

	public void setAvailableSkills(List<Skill> skills) {
		Object oldValue = getAvailableSkills();
		this.availableSkills = skills;
		notifier.firePropertyChange("availableSkills", oldValue, selectedPerson);
	}

	public List<Skill> getAvailableSkills() {
		return availableSkills;
	}

}
