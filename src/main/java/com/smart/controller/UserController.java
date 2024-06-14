package com.smart.controller;

import java.security.Principal;
import java.util.List;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.razorpay.*;

import jakarta.servlet.http.HttpSession;
import com.smart.dao.MyOrderRepository;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bcryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepostiory;
	
	@Autowired ContactRepository contactRepository;
	
	@Autowired
	private MyOrderRepository myOderRepository;
	
	
	
	
	
	//method for adding common data to response
	@ModelAttribute // this will use by all handler
     void addCommonData(Model m,Principal principal) {
		String userName=principal.getName();// it give username(email)
		
		User u=this.userRepostiory.getUserByUserName(userName);
		System.out.println("user"+u);
		m.addAttribute("userName", u);
	}
	
	@RequestMapping("/index")
	public String dashboard(Model m,Principal princ) {
		m.addAttribute("title","User Dashboard ");

		return "user_dasboard";
	}
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model m) {
		m.addAttribute("title","Add Contact ");
		m.addAttribute("contact", new Contact());
		return "add_contact_form_user";
	}
	// principal is use to get user data(username)
	// processing and contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, Principal principal,HttpSession session)
	{
		
		try {
		String name=principal.getName();
		User user=this.userRepostiory.getUserByUserName(name);
		
		//processing and uploading file..
		if(file.isEmpty()) {
			
			//if the file is empty then try our message
			System.out.println("File is empty");
			contact.setImage("contact.jpg");
		}
		else {
			contact.setImage(file.getOriginalFilename());
			File saveFile=new ClassPathResource("static/img").getFile().getAbsoluteFile();
			Files.copy(file.getInputStream(),Paths.get(saveFile+File.separator+file.getOriginalFilename()),StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Image is uploaded");
		}
		
		
		contact.setUser(user);
		
		user.getContact().add(contact);
		
		this.userRepostiory.save(user);
		
		System.out.println("Added to data base");
		
		//message success....
		session.setAttribute("message", new Message("your contact is added !! ","success"));
//		System.out.println("DATA"+contact);
//		return "add_contact_form_user";
		}catch(Exception e) {
			e.printStackTrace();
			// message error....
			session.setAttribute("message", new Message("Something went wrong !! ","danger"));

		}
		return "add_contact_form_user";
	  }
	
	//show contacts handler
	// per page 5[n]
	//current page =0[page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") int page,Model m,Principal principal) {
		m.addAttribute("title","Show User Contacts ");
		
		String username=principal.getName();
		
		User user=this.userRepostiory.getUserByUserName(username);
		Pageable pageable=PageRequest.of(page, 2);
		Page<Contact> contacts=this.contactRepository.findContactsByUser(user.getId(),pageable);
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		return "show_contacts";
	}

	//showing particular contact details.
	@GetMapping("{cId}/contact")
	public String showContactDetail(@PathVariable("cId") int cId,Model m,Principal p) {
		Optional<Contact> contactOptional=this.contactRepository.findById(cId);
		Contact contact=contactOptional.get();
		
		String username=p.getName();
		User user=this.userRepostiory.getUserByUserName(username);
		
		if(user.getId()==contact.getUser().getId()) {
		
			m.addAttribute("contact", contact);
			m.addAttribute("title",contact.getName());
			}
		return "user_contact_details";
	}
	
	//delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid")Integer cId,Model m,Principal p,HttpSession session) {
		
		Contact contact =this.contactRepository.findById(cId).get();
		
		
		//check..
		String username=p.getName();
		User user=this.userRepostiory.getUserByUserName(username);
		
		if(user.getId()==contact.getUser().getId()) {
		
//			contact.setUser(null);
			m.addAttribute("title",contact.getName());
			
//			this.contactRepository.delete(contact);
			
			
			User user1 =this.userRepostiory.getUserByUserName(p.getName());
			
			user1.getContact().remove(contact);
			
			this.userRepostiory.save(user);
			
			session.setAttribute("message", new Message("Contact deleted successfully..","success"));
			}
		
		return"redirect:/user/show-contacts/0";
	}
	
	//open update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid ,Model m) {
		m.addAttribute("title","Update Contact");
		Contact contact=this.contactRepository.findById(cid).get();
		m.addAttribute("contact", contact);
		return "user_update_form";
	}
	
	//udate contact handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model m,Principal principal,HttpSession session) {
		
		System.out.println(file.getOriginalFilename());
		try {
			
			//old contact details
			Contact oldcontactDetail=this.contactRepository.findById(contact.getcId()).get();
			//image..
			if(!file.isEmpty()) {
				
				//if the file is empty then try our message
//				delete old photo
				File deleteFile=new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile,oldcontactDetail.getImage());
				file1.delete();
//				update new photo
				File saveFile=new ClassPathResource("static/img").getFile().getAbsoluteFile();
				
				Files.copy(file.getInputStream(),Paths.get(saveFile+File.separator+file.getOriginalFilename()),StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}
			else {
				contact.setImage(oldcontactDetail.getImage());
			}
			
			User user=this.userRepostiory.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your contact is updated..","success"));
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("contact name"+contact.getName());
		return"redirect:/user/"+contact.getcId()+"/contact";
	}
	
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model m,Principal p) {
		m.addAttribute("title", "Profile Page");
		String username=p.getName();
		User user=this.userRepostiory.getUserByUserName(username);
		m.addAttribute("user", user);
		return "user_profile";
	}
	
	//open setting handler
	@GetMapping("/settings")
	public String openSetting() {
		return "settings";
	}
	
	//change password ..handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword")String oldPassword, @RequestParam("newPassword") String newPassword,Principal principal,HttpSession session ) {
		System.out.println("oldPassword"+oldPassword);
		System.out.println("newPassword"+newPassword);
		session.setAttribute("message", new Message("Your password is successfully changed..","success"));
		String userName=principal.getName();
		User u=this.userRepostiory.getUserByUserName(userName);
		if(this.bcryptPasswordEncoder.matches(oldPassword, u.getPassword())) {
			//change the password
			u.setPassword(this.bcryptPasswordEncoder.encode(newPassword));
			this.userRepostiory.save(u);
			
		}
		else {
			//errors
			session.setAttribute("message", new Message("Could Your Please Enter Correct Old Password !!","danger"));
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/index";
	}
	
	//creating order for payment
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data,Principal principal) throws Exception {
		System.out.println(data);
		int amt= Integer.parseInt(data.get("amount").toString());
		
		var client=new RazorpayClient("rzp_test_YL6yJjiMoXnrwY","uabzcaJjmmmGsTMmpkhAe1PR");
		
		JSONObject ob=new JSONObject();
		ob.put("amount", amt*100);
		ob.put("currency","INR");
		ob.put("receipt","txn_235425");
		
		
		//creating new order
		Order order=client.orders.create(ob);
		System.out.println(order);
		
		//save the order in database
		
		MyOrder myOrder =new MyOrder();
		
		myOrder.setAmount(order.get("amount")+"");
		myOrder.setOrderId(order.get("id"));
		myOrder.setPaymentId(null);
		myOrder.setStatus("created");
		myOrder.setUser(this.userRepostiory.getUserByUserName(principal.getName()));
		myOrder.setReceipt(order.get("receipt"));
		this.myOderRepository.save(myOrder);
		
		
		// if you want you can save this to your data..
		
		return order.toString();
	}
	
	
	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String,Object> data){
		
		MyOrder myorder = this.myOderRepository.findByOrderId(data.get("order_id").toString());
		myorder.setPaymentId(data.get("payment_id").toString());
		myorder.setStatus(data.get("status").toString());
		this.myOderRepository.save(myorder);
		
		
		System.out.println(data);
		return ResponseEntity.ok(Map.of("msg","updated"));
	}
}
