package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;


@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired 
	private UserRepository userRepository;
	
	@GetMapping("/")
	public String home(Model m) {
		m.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}
	
	@GetMapping("/about")
	public String about(Model m) {
		m.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}
	
	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "Register - Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}
	
	//***************** handler for registering user *******************************
	
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1,@RequestParam(value="agreement",defaultValue="false")boolean agreement,Model model,HttpSession session)
	{
		try {
		if(!agreement) {
			System.out.println("you have not agreed the terms and conditions"); 
			//throw new Exception("you have not agreed the terms and conditions");
		}
		if(result1.hasErrors())
		{
			System.out.println("Error"+result1.toString());
			model.addAttribute("user", user);
			return "signup";
		}
		user.setRole("ROLE_USER");
		user.setEnabled(true);
		user.setImageUrl("default.png");
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		
		System.out.println("Agreement "+agreement);
		System.out.println("user "+user);
		User result=this.userRepository.save(user);
		
		model.addAttribute("user", new User());
		session.setAttribute("message",new Message("Successfully Registered !!","alert-success"));
		return "signup";
		
		}catch(Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message",new Message("Something Went Wrong!!"+e.getMessage(),"alert-danger"));
		}
		return "signup";
	}

	//handle for custom login
	@GetMapping("/signin")
	public String customLogin(Model m) {
		m.addAttribute("title", "SignIn - Smart Contact Manager");
		return "login";
	}
}