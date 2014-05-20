package org.vaadin.exampleapp.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.vaadin.exampleapp.data.Person;
import org.vaadin.spring.navigator.VaadinView;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

@VaadinView(name = "")
@SuppressWarnings("serial")
@Scope("ui")
public class PersonView extends VerticalSplitPanel implements View,
		ValueChangeListener, PropertyChangeListener {

	@Autowired
	private PersonPresenter presenter;

	private final Table personTable = new Table();

	private final VerticalLayout formContent = new VerticalLayout();

	private final BasicDetailsForm basicDetailsForm = new BasicDetailsForm();
	private final AddressForm addressForm = new AddressForm();
	private final SkillsForm skillsForm = new SkillsForm();

	private final FieldGroup binder = new FieldGroup();

	private PersonModel model;

	private TabSheet tabs;

	private Button addBtn = new Button("+", new ClickListener() {

		public void buttonClick(ClickEvent event) {
			presenter.addButtonClicked();
		}
	});

	private Button removeBtn = new Button("-", new ClickListener() {

		public void buttonClick(ClickEvent event) {
			presenter.removeButtonClicked();
		}
	});

	public PersonView() {
		VerticalLayout top = new VerticalLayout();
		top.setSizeFull();

		HorizontalLayout toolbar = new HorizontalLayout();

		toolbar.addComponent(addBtn);
		toolbar.addComponent(removeBtn);
		toolbar.setSpacing(true);

		top.addComponent(toolbar);
		top.addComponent(personTable);
		top.setExpandRatio(personTable, 1);
		addComponent(top);
		personTable.addValueChangeListener(this);
		personTable.setSizeFull();
		personTable.setSelectable(true);
		personTable.setImmediate(true);

		tabs = new TabSheet();
		basicDetailsForm.setCaption("Basic details");
		tabs.addComponent(basicDetailsForm);

		addressForm.setCaption("Address");
		tabs.addComponent(addressForm);

		skillsForm.setCaption("Skills");
		tabs.addComponent(skillsForm);

		tabs.setSizeFull();
		formContent.addComponent(tabs);

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setMargin(true);
		buttonLayout.setSpacing(true);

		Button saveBtn = new Button("Save changes", new ClickListener() {

			public void buttonClick(ClickEvent event) {
				presenter.saveClicked();
			}
		});
		buttonLayout.addComponent(saveBtn);

		Button cancelBtn = new Button("Discard changes", new ClickListener() {

			public void buttonClick(ClickEvent event) {
				presenter.cancelClicked();
			}
		});
		buttonLayout.addComponent(cancelBtn);
		formContent.addComponent(buttonLayout);
		formContent.setVisible(false);
		formContent.setSizeFull();
		formContent.setExpandRatio(tabs, 1);

		setSplitPosition(100);
		addComponent(formContent);
	}

	@PostConstruct
	public void init() {
		presenter.setView(this);
	}

	public void enter(ViewChangeEvent event) {
		presenter.enter();
	}

	public void setModel(PersonModel model) {
		this.model = model;
		model.addPropertyChangeListener(this);
	}

	public void valueChange(ValueChangeEvent event) {
		presenter.personSelected((Person) event.getProperty().getValue());
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if ("persons".equals(evt.getPropertyName())) {
			updateTableContent();
		} else if ("selectedPerson".equals(evt.getPropertyName())) {
			if (evt.getNewValue() == null) {
				hideForm();
				return;
			}

			showForm();

			if (isNewPerson(evt)) {
				personTable.setValue(null);
				BeanItem<Person> item = new BeanItem<Person>(
						(Person) evt.getNewValue());
				item.addNestedProperty("address.street");
				item.addNestedProperty("address.zip");
				item.addNestedProperty("address.city");

				binder.setItemDataSource(item);
				bindFormLayouts();
				return;
			}

			if (evt.getNewValue() != null) {
				personTable.setValue(evt.getNewValue());
				Item item = personTable.getItem(evt.getNewValue());
				binder.setItemDataSource(item);
				bindFormLayouts();
			}
		} else if ("availableSkills".equals(evt.getPropertyName())) {
			skillsForm.setAvailableSkills(model.getAvailableSkills());
		}
	}

	private boolean isNewPerson(PropertyChangeEvent evt) {
		return !personTable.containsId(evt.getNewValue());
	}

	private void showForm() {
		setSplitPosition(50);
		formContent.setVisible(true);
		tabs.setSelectedTab(0);
	}

	private void hideForm() {
		formContent.setVisible(false);
		personTable.setValue(null);
		setSplitPosition(100);
	}

	private void updateTableContent() {
		BeanItemContainer<Person> container = new BeanItemContainer<Person>(
				Person.class);
		container.addNestedContainerBean("address");
		container.addAll(model.getPersons());
		personTable.setContainerDataSource(container);
		personTable.setVisibleColumns(new Object[] { "firstname", "lastname",
				"email", "title", "skills" });
	}

	private void bindFormLayouts() {
		binder.bindMemberFields(basicDetailsForm);
		binder.bindMemberFields(addressForm);
		binder.bindMemberFields(skillsForm);
	}

	public void discardChanges() {
		binder.discard();
	}

	public void saveChanges() throws CommitException {
		clearTabCaptionsFromErrorIndicator();

		for (Field<?> f : binder.getFields()) {
			try {
				f.validate();
				f.removeStyleName("invalid-value");
			} catch (InvalidValueException e) {
				f.addStyleName("invalid-value");
				Tab tab = findTabForField(f);
				addErrorIndicatorToTabCaption(tab);
			}
		}

		binder.commit();
	}

	private Tab findTabForField(Field<?> f) {
		Tab tab = tabs.getTab(f.getParent());
		return tab;
	}

	private void addErrorIndicatorToTabCaption(Tab tab) {
		if (!tab.getCaption().contains("(!)")) {
			tab.setCaption(tab.getCaption() + " (!)");
		}
	}

	private void clearTabCaptionsFromErrorIndicator() {
		tabs.getTab(basicDetailsForm).setCaption(
				tabs.getTab(basicDetailsForm).getCaption().replace(" (!)", ""));
		tabs.getTab(addressForm).setCaption(
				tabs.getTab(addressForm).getCaption().replace(" (!)", ""));
	}
}
