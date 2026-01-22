$(function() {
	const ak = 'BMXS3lDH7kKVzDdH55nP5OzgOM1NECCl';
	function getLocation() {
		const resultDiv = document.getElementById('result');
		resultDiv.innerHTML = '正在获取位置信息...';
		const url = `http://api.map.baidu.com/location/ip?ak=${ak}&coor=bd09ll`;
		$.ajax({
			url,
			type: 'POST',
			dataType: 'jsonp',
			success: function(data) {
				// console.log(data, 'data');
				if (data.status === 0) {
					$.ajax({
						url: 'http://ip-api.com/json',
						success: function(response) {
							data.ip = response.query
							record(data, "gps")
							const point = data.content.point;
							const address = data.content.address;
							resultDiv.innerHTML = `
										    <p><strong>IP地址:</strong> ${data.ip}</p>
										    <p style="text-decoration: underline;cursor: pointer"
										     onClick='setData(${point.x},${point.y})'><strong>经纬度:</strong> 经度 ${point.x}, 纬度 ${point.y}</p>
										    <p><strong>详细地址:</strong> ${address}</p>
										    <p><strong>城市:</strong> ${data.content.address_detail.city}</p>
											<p><strong>区:</strong> ${data.content.address_detail.district}</p>
										    <p><strong>省份:</strong> ${data.content.address_detail.province}</p>
										`;
						},
						type: 'GET',
						dataType: 'JSON'
					});
				} else {
					resultDiv.innerHTML = `<p style="color:red;">获取位置失败: ${data.message}</p>`;
				}
			}
		});
	}

	function setData(x, y) {
		window.location.location1 = x;
		window.location.location2 = y;
		localStorage.setItem("location1", x)
		localStorage.setItem("location2", y)
		window.location.href = "./map.html"
	}

	function getLocationById() {
		const resultDiv = document.getElementById('result');
		resultDiv.innerHTML = '正在获取位置信息...';
		$.ajax({
			url: 'http://ip-api.com/json',
			success: function(response) {
				const ip = response.query
				const url = `http://api.map.baidu.com/location/ip?ip=${ip}&ak=${ak}&coor=bd09ll`;
				$.ajax({
					url,
					type: 'POST',
					dataType: 'jsonp',
					success: function(data) {
						if (data.status === 0) {
							// console.log(data, 'data');
							record(data, "ip")
							const point = data.content.point;
							const address = data.content.address;
							resultDiv.innerHTML = `
	                                    <p><strong>IP地址:</strong> ${ip}</p>
	                                    <p style="text-decoration: underline;cursor: pointer"
	                                    onClick='setData(${point.x},${point.y})'><strong>经纬度:</strong> 经度 ${point.x}, 纬度 ${point.y}</p>
	                                    <p><strong>详细地址:</strong> ${address}</p>
	                                    <p><strong>城市:</strong> ${data.content.address_detail.city}</p>
										<p><strong>区:</strong> ${data.content.address_detail.district}</p>
	                                    <p><strong>省份:</strong> ${data.content.address_detail.province}</p>
	                                `;
						} else {
							resultDiv.innerHTML =
								`<p style="color:red;">获取位置失败: ${data.message}</p>`;
						}
					}
				});
			},
			type: 'GET',
			dataType: 'JSON'
		});
	}

	function record(locationData, type) {
		const url = `/api/uploadLocation`;
		fetch(url, {
				method: 'POST', // 指定请求方法
				// options:{},
				headers: {
					'Content-Type': 'application/json'
					// 'Authorization': 'Bearer your-token'
				},
				body: JSON.stringify({
					type,
					locationData: JSON.stringify(locationData)
				})
			})
			.then(response => {
				if (!response.ok) {
					throw new Error(`HTTP error! status: ${response.status}`);
				}
				return response.text();
			})
			.then(result => {
				// console.log(result, "result")
				result = JSON.parse(result)
				if (result.code === 200) {
					if (result.data.record) {
						// console.log("上传成功！")
						message(type,result.data)
					} else {
						// console.log("上传不成功！")
					}
				}
				return result;
			})
			.catch(error => {
				// console.error('上传失败:', error);
				throw error;
			});
	}

	getLocation()
	getLocationById()
})