package pet.store.service;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pet.store.controller.model.PetStoreData;
import pet.store.controller.model.PetStoreData.PetStoreCustomer;
import pet.store.controller.model.PetStoreData.PetStoreEmployee;
import pet.store.dao.CustomerDao;
import pet.store.dao.EmployeeDao;
import pet.store.dao.PetStoreDao;
import pet.store.entity.Customer;
import pet.store.entity.Employee;
import pet.store.entity.PetStore;

@Service
public class PetStoreService {

	@Autowired
	private PetStoreDao petStoreDao;
	
	@Autowired
	private EmployeeDao employeeDao;
	
	@Autowired
	private CustomerDao customerDao;
	

	@Transactional(readOnly = false)
	public PetStoreData savePetStore(PetStoreData petStoreData) {
		Long petStoreId = petStoreData.getPetStoreId();
		PetStore petStore = findOrCreatePetStore(petStoreId);
		copyPetStoreFields(petStore, petStoreData);
		return new PetStoreData(petStoreDao.save(petStore));
		
	}
	
	private void copyPetStoreFields(PetStore petStore, PetStoreData petStoreData) {
		petStore.setPetStoreName(petStoreData.getPetStoreName());
		petStore.setPetStoreAddress(petStoreData.getPetStoreAddress());
		petStore.setPetStoreCity(petStoreData.getPetStoreCity());
		petStore.setPetStoreState(petStoreData.getPetStoreState());
		petStore.setPetStoreZip(petStoreData.getPetStoreZip());
		petStore.setPetStorePhone(petStoreData.getPetStorePhone());
	}

	private PetStore findOrCreatePetStore(Long petStoreId) {
		PetStore petStore;
		
		if (Objects.isNull(petStoreId)) {
			petStore = new PetStore();
		} else {
			petStore = findPetStoreById(petStoreId);
		}
		return petStore;
	}
	
	private PetStore findPetStoreById(Long petStoreId) {
		return petStoreDao.findById(petStoreId).orElseThrow(() -> new NoSuchElementException("Pet store with ID="+petStoreId+ " was not found"));
	}
	
	@Transactional(readOnly = false)
	public PetStoreEmployee saveEmployee(PetStoreEmployee petStoreEmployee, Long petStoreId) {
		PetStore petStore = findPetStoreById(petStoreId);
		Employee employee = findOrCreateEmployee(petStoreId, petStoreEmployee.getEmployeeId());
		copyEmployeeFields(employee, petStoreEmployee);
		employee.setPetStore(petStore);
		petStore.getEmployees().add(employee);
		return new PetStoreEmployee(employeeDao.save(employee));
	}

	private Employee findOrCreateEmployee(Long petStoreId, Long employeeId) {
		Employee employee;
		
		if (Objects.isNull(employeeId)) {
			employee = new Employee();
		} else {
			employee = findEmployeeById(petStoreId, employeeId);
		}
		return employee;
	}
	
	private Employee findEmployeeById(Long petStoreId, Long employeeId) {
		Employee employee = employeeDao.findById(employeeId).orElseThrow(() -> new NoSuchElementException("Employee with ID="+employeeId+ " was not found"));
		if (employee.getPetStore().getPetStoreId() != petStoreId) 
			throw new IllegalArgumentException("Pet store id associated with employee=" 
					+ employee.getPetStore().getPetStoreId() + " does not equal specified pet store id " + petStoreId);
		return employee;
	}
	
	private void copyEmployeeFields(Employee employee, PetStoreEmployee petStoreEmployee) {
		employee.setEmployeeFirstName(petStoreEmployee.getEmployeeFirstName());
		employee.setEmployeeLastName(petStoreEmployee.getEmployeeLastName());
		employee.setEmployeePhone(petStoreEmployee.getEmployeePhone());
		employee.setEmployeeJobTitle(petStoreEmployee.getEmployeeJobTitle());
	}

	@Transactional(readOnly = false)
	public PetStoreCustomer saveCustomer(PetStoreCustomer petStoreCustomer, Long petStoreId) {
		PetStore petStore = findPetStoreById(petStoreId);
		Customer customer = findOrCreateCustomer(petStoreId, petStoreCustomer.getCustomerId());
		copyCustomerFields(customer, petStoreCustomer);
		customer.getPetStores().add(petStore);
		petStore.getCustomers().add(customer);
		return new PetStoreCustomer(customerDao.save(customer));
	}
		
	private Customer findOrCreateCustomer(Long petStoreId, Long customerId) {
		Customer customer;
		
		if (Objects.isNull(customerId)) {
			customer = new Customer();
		} else {
			customer = findCustomerById(petStoreId, customerId);
		}
		return customer;
	}
	
	private Customer findCustomerById(Long petStoreId, Long customerId) {
		Customer customer = customerDao.findById(customerId).orElseThrow(() -> 
			new	NoSuchElementException("Customer with ID="+customerId+ " was not found"));

		for (PetStore petStore : customer.getPetStores()) { 
			if (petStore.getPetStoreId() == petStoreId)
				return customer; 
		} 
		throw new IllegalArgumentException("Customer is not associated with pet store id=" + petStoreId);
	}
	
	private void copyCustomerFields(Customer customer, PetStoreCustomer petStoreCustomer){
		customer.setCustomerFirstName(petStoreCustomer.getCustomerFirstName());
		customer.setCustomerLastName(petStoreCustomer.getCustomerLastName());
		customer.setCustomerEmail(petStoreCustomer.getCustomerEmail());
	}

	@Transactional
	public List<PetStoreData> retrieveAllPetStores() {
		List<PetStoreData> result = new LinkedList<>();
		for (PetStore petStore : petStoreDao.findAll()) {
			PetStoreData psd = new PetStoreData(petStore);
			
			psd.getCustomers().clear();
			psd.getEmployees().clear();
			
			result.add(psd);
		}
		return result;
	}

	@Transactional
	public PetStoreData retrievePetStoreById(Long petStoreId) {
		return new PetStoreData(findPetStoreById(petStoreId));
	}

	public void deletePetStoreById(Long petStoreId) {
		petStoreDao.delete(findPetStoreById(petStoreId)); 
	}
}
