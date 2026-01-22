let deviceData = {};
document.addEventListener('DOMContentLoaded', function() {
	// 获取设备信息
	const userAgent = navigator.userAgent;
	deviceData.userAgent = userAgent;
	const platform = navigator.platform;
	deviceData.platform = platform;
	const language = navigator.language;
	deviceData.language = language;
	const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(userAgent);
	const isTablet = /iPad|Android(?!.*mobile)|Tablet|Silk/i.test(userAgent);

	// 设备类型判断
	let deviceType = '电脑';
	let deviceIcon = 'desktop';
	let deviceName = '桌面设备';

	if (isMobile) {
		deviceType = '手机';
		deviceIcon = 'mobile-alt';
		deviceName = '移动设备';
	}

	if (isTablet) {
		deviceType = '平板';
		deviceIcon = 'tablet-alt';
		deviceName = '平板设备';
	}
	deviceData.deviceType = deviceType;
	deviceData.deviceIcon = deviceIcon;
	deviceData.deviceName = deviceName;
	// 操作系统检测
	let os = '未知';
	if (userAgent.indexOf('Win') !== -1) os = 'Windows';
	if (userAgent.indexOf('Mac') !== -1) os = 'Mac OS';
	if (userAgent.indexOf('Linux') !== -1) os = 'Linux';
	if (userAgent.indexOf('Android') !== -1) os = 'Android';
	if (userAgent.indexOf('iOS') !== -1) os = 'iOS';
	deviceData.os = os;
	// 浏览器检测
	let browser = '未知';
	if (userAgent.indexOf('Chrome') !== -1 && userAgent.indexOf('Edg') === -1) browser = 'Chrome';
	if (userAgent.indexOf('Firefox') !== -1) browser = 'Firefox';
	if (userAgent.indexOf('Safari') !== -1 && userAgent.indexOf('Chrome') === -1) browser = 'Safari';
	if (userAgent.indexOf('Edg') !== -1) browser = 'Edge';
	if (userAgent.indexOf('Opera') !== -1 || userAgent.indexOf('OPR') !== -1) browser = 'Opera';
	deviceData.browser = browser;
	// 屏幕信息
	const screenWidth = screen.width;
	deviceData.screenWidth = screenWidth;
	const screenHeight = screen.height;
	deviceData.screenHeight = screenHeight;
	const colorDepth = screen.colorDepth;
	deviceData.colorDepth = colorDepth;
	const pixelRatio = window.devicePixelRatio;
	deviceData.pixelRatio = pixelRatio;

	// 浏览器窗口信息
	const windowWidth = window.innerWidth;
	deviceData.windowWidth = windowWidth;
	const windowHeight = window.innerHeight;
	deviceData.windowHeight = windowHeight;

	// 设备方向
	const orientation = window.innerWidth > window.innerHeight ? '横屏' : '竖屏';
	deviceData.orientation = orientation;
	// 在线状态
	const onlineStatus = navigator.onLine ? '在线' : '离线';
	deviceData.onlineStatus = onlineStatus;
	// 填充设备卡片
	const deviceCard = document.getElementById('deviceCard');
	deviceCard.className = `device-card ${isMobile || isTablet ? 'mobile' : 'desktop'}`;
	deviceCard.innerHTML = `
                <div class="device-header">
                    <div class="device-icon">
                        <i class="fas fa-${deviceIcon}"></i>
                    </div>
                    <div class="device-info">
                        <h2>${deviceType}设备</h2>
                        <p>检测到您正在使用${deviceName}</p>
                    </div>
                </div>
                <p>您的设备已被识别为 <strong>${deviceType}</strong>，以下为详细信息：</p>
            `;

	// 填充信息网格
	const infoGrid = document.getElementById('infoGrid');
	infoGrid.innerHTML = `
                <div class="info-item">
                    <span class="info-label">设备类型</span>
                    <span class="info-value">${deviceType}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">操作系统</span>
                    <span class="info-value">${os}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">浏览器</span>
                    <span class="info-value">${browser}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">用户代理</span>
                    <span class="info-value">${userAgent.substring(0, 50)}...</span>
                </div>
                <div class="info-item">
                    <span class="info-label">平台</span>
                    <span class="info-value">${platform}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">语言</span>
                    <span class="info-value">${language}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">屏幕分辨率</span>
                    <span class="info-value">${screenWidth} × ${screenHeight}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">窗口大小</span>
                    <span class="info-value">${windowWidth} × ${windowHeight}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">颜色深度</span>
                    <span class="info-value">${colorDepth} 位</span>
                </div>
                <div class="info-item">
                    <span class="info-label">设备像素比</span>
                    <span class="info-value">${pixelRatio}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">设备方向</span>
                    <span class="info-value">${orientation}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">网络状态</span>
                    <span class="info-value">${onlineStatus}</span>
                </div>
            `;
});