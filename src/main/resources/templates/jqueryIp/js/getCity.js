$(function(){
	
//获取城市ajax
	$.ajax({
	url: 'http://api.map.baidu.com/location/ip?ak=BMXS3lDH7kKVzDdH55nP5OzgOM1NECCl',  
	type: 'POST',  
	dataType: 'jsonp',
	success:function(data) {  
		console.log(JSON.stringify(data.content.address_detail.province + "," + data.content.address_detail.city));
		$('#city').html(JSON.stringify(data.content.address_detail.province + "," + data.content.address_detail.city))
	}
	});
	//获取ip ajax
	$.ajax({
	    url: 'http://ip-api.com/json',
	    success: function(data){
	       console.log(JSON.stringify(data.query));
	       $('#ip').html(JSON.stringify(data.query))
	    },
	    type: 'GET',
	    dataType: 'JSON'
	});
})

