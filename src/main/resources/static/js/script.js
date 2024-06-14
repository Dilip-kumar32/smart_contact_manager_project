console.log("this");


const toggleSidebar = () => {
	if($(".sidebar").is(":visible")){

		$(".sidebar").css("display","none");
		$(".content").css("margin-left","0%");
	}else{
		$(".sidebar").css("display","block");
		$(".content").css("margin-left","20%");
	}
};

const search = () => {
	console.log("this is for testing1");
	var b=document.getElementById("search-result");
	var a=document.getElementById("search-input");
    console.log(a.value);
            
              
                let url=`http://localhost:8080/search/${a.value}`;
               
                fetch(url)
                .then((response) =>{
					return response.json();
				})
				.then((data) =>{
					//data......
					console.log(data);
					let text=`<div class='list-group'>`
					
					data.forEach((contact) =>{
						text+=`<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-actin'> ${contact.name}</a>`
					});
					text+`</div>`;
					b.innerHTML=text;
					 b.style.display="block";
					
				});
            
    
    	
    	if(a.value==="")
    	{
			b.style.display="none";
		}
            
	
}

const fun=()=>{
	
	let addOb=document.getElementById("add")
	let addcl=document.getElementById("add").classList.contains('container');
	if(addcl){
	addOb.classList.remove('container');
	addOb.classList.add("container-fluid");
	
	}
	else{
		addOb.classList.remove('container-fluid');
	addOb.classList.add("container");
	}
		
};

//first request to server to create order

const paymentStar=()=>{
	console.log("payment started")
	let amount=document.getElementById("payment_field").value;
	console.log(amount);
	if(amount == "" || amount==null){
		//alert("Amount is Required !!");
	swal("Failed", "Amount is Required !!", "error");
		return;
	}
	//code...
	//we will use ajax to send request to server to create order- jquery
	$.ajax({
		url: '/user/create_order',
		data:JSON.stringify({amount:amount,info:'order_request'}),
		contentType:'application/json',
		type:'POST',
		dataType:'json',
		success:function(response){
			//invoked where success
			console.log(response);
			if(response.status =="created"){
				//open payment form
				let options={
					key:'rzp_test_YL6yJjiMoXnrwY',
					amount:response.amount,
					currency: 'INR',
					name:'Smart Contact Manager',
					description:'Donation',
					image:'data:image/png;base64,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',
					order_id:response.id,
					handler:function(response){
						console.log(response.razorpay_payment_id);
						console.log(response.razorpay_order_id);
						console.log(response.razorpay_signature);
						console.log('payment successful !!');
						//alert('congrates !! Payment successful !!');
						updatePaymentOnServer(response.razorpay_payment_id,response.razorpay_order_id,'paid');
						
					},
					"prefill":{
						"name":"",
						"email":"",
						"contact":""
					},
					notes:{
						address: "Razorpay Corporate Office",
					},
					theme:{
						color:"#3399cc",
					},
						
				};
				
				let rzp=new Razorpay(options);
				
				rzp.on('payment.faild',function(response){
					console.log(response.error.code);
					console.log(response.error.description);
					console.log(response.error.source);
					console.log(response.error.step);
					console.log(response.error.reason);
					console.log(response.error.metadata.order_id);
					console.log(response.error.metadata.payment_id);
					//alert("Oops payment failed !!");
					swal("Failed", "Oops payment failed !!", "error");
					
					});
	
				rzp.open();
				
			}
		},
		error:function(error){
			//invoked when error
			console.log(error)
			alert("something went wrong !!")
		}
	})
	
};

function updatePaymentOnServer(payment_id,order_id,status){
	$.ajax({
		url: '/user/update_order',
		data:JSON.stringify({
			payment_id: payment_id, 
			order_id: order_id, 
			status: status,
		}),
		contentType:'application/json',
		type:'POST',
		dataType:'json',
		success: function(response){
			swal("THANK YOU!", "congrates !! Payment successful !!", "success");
		},
		error: function(error){
			swal("Failed", "Your payment is successful , but we did not get on server, we will contact you as soon as possible", "error");
		},
	});
}
