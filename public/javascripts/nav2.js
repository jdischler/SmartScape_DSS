Ext.Loader.setConfig({
	enabled: true,
	paths: {
		'GeoExt': '/assets/javascripts/vendor/geo-ext'
	}
});

var test = "254.16 88.36 288.05 101.72 292.33 104.32 296.29 107.45 300.16 111.36 304.56 116.95 308.13 122.75 310.97 128.68 314.91 140.59 320.38 175.8 320.41 182.31 319.79 188.83 318.32 195.25 315.84 201.46 312.16 207.36 310.4 209.54 307.33 212.69 302.92 216.18 300.21 217.85 297.16 219.36 293.35 220.75 289.62 221.62 285.93 222.05 278.57 221.9 258.13 219.23 228.24 218.15 223.16 217.36 215.38 215.42 208.79 212.77 202.66 209.26 125.87 155.76 112.03 148.15 105.87 145.5 99.97 143.58 94.16 142.36 84.88 141.52 76.86 141.72 57.12 144.38 50.92 144.56 44.37 143.69 37.16 141.36 31.11 138.36 25.49 134.63 20.33 130.27 15.69 125.38 11.61 120.05 8.13 114.37 5.3 108.44 3.16 102.36 1 87.36 2.72 73.25 7.27 60.31 13.62 48.83 20.73 39.11 33.04 26.09 44.25 17.36 53.27 11.95 62.93 7.36 72.92 3.85 82.95 1.64 92.69 1 101.87 2.16 110.16 5.36 112.43 6.82 132.78 22.67 254.16 88.36 254.16 88.36"
var ar = test.split(' ');
var newStr = "";
for (var i = 0; i < ar.length; i = i + 2) {
	newStr += ar[i] + "," + ar[i+1] + " ";
}

var wisc_1 = 'M369.22,539.06 199.5,538.08 195.11,537.23 192.57,533.57 189.39,526.37 183.69,521.58 176.29,519.73 169.02,517.35 162.13,513.72 157,508.06 156.26,501.34 154.73,495.19 147.52,476.44 156.38,448.63 150.52,441.36 143.44,426.93 140.73,409.17 139.55,394.17 137,378.89 131.69,370.75 125.88,362.96 120.73,359.9 114.79,358.45 108.69,354.64 103.43,349.75 91.65,340.52 84.53,327.76 75.44,315.42 60.61,309.38 44.59,293.98 29.02,290.51 9.34,273 8.14,267.24 10.06,260.81 11.34,247.06 8.05,234.07 9.97,229.03 11.64,222.78 11.71,212.18 14.83,202.18 18.94,195.37 20.54,187.83 16.85,180.15 12.08,172.95 1.31,170.07 0,162.89 1.77,155.65 10.91,142.58 14.11,134.27 20.6,128.52 27.38,124.51 34.8,122.29 38.31,118.69 44.08,117.66 44.8,117.48 45.39,116.96 47.54,112.64 50.13,108.55 51.27,102.38 50.93,48.02 55.2,38.53 58.78,36.12 62.4,33.32 64.72,28.67 68.53,28.56 81.05,35.07 93.97,31.93 154.44,11.04 163.14,4.07 173.41,0 179.6,7 175.98,16.88 171.9,21.39 170.75,25.12 171.97,28.89 170.13,33.3 167.45,37.99 167.6,45.36 176.31,42.88 185.49,37.47 210.33,50.04 226.79,56.78 231.54,62.97 234.37,70.5 243.19,78.78 320.98,100.84 333.92,106.73 345.96,114.33 351.29,116.16 356.62,117.09 362.45,120.22 366.78,119.72 370.89,117.99 374.45,118.47 377.91,119.91 396.65,123.42 410.9,133.01 411.2,135.24 410.26,137.16 408.14,139.25 408.1,141.91 415.28,145.52 425.05,149.43 428.34,149.82 435.72,155.76 436.44,156.93 436.59,157.97 435.14,162.46 437.4,166.44 436.57,169.21 435.77,171.1 436.25,176.57 435.64,182.34 430.37,191.39 432.62,199.7 441.56,198.05 448.48,197.42 445.8,207.66 441.94,218.86 447.97,226.23 452.36,233 451.67,236.07 451.21,239.19 446.11,243.71 438.53,245.01 434.09,247.65 432.99,252.71 429.26,262.68 422.79,271.69 417.41,285.21 419.1,297.82 423.77,299.56 427.51,297.38 437.52,288.02 445.4,278.35 450.09,268.9 456.05,261.56 461.33,261.79 467.04,258.31 468.54,257.72 473.79,263.41 476.98,270.48 472.15,281.47 466.51,292 463.14,303.67 458.88,324.46 460.97,333.31 460.12,340.62 453.99,345.65 449.59,351.55 447,358.5 443.55,370.32 442.42,382.61 443.9,393.93 441.86,405.27 438.34,417.49 429.12,441.26 427.65,454.36 429.82,466.02 428.23,477.23 428.06,479.96 429.84,482.44 435.73,502.35 438.79,508.93 437.55,515.38 436.13,529.78 430.04,539.39 369.22,539.29 369.22,539.06 369.22,539.06z';
var wisc_2 = 'M507.33,206.32 506.94,209.29 506.26,212.18 504.36,213.16 502.35,215.25 498.93,225.54 495.39,232.27 492.37,239.13 489.48,249.31 484.28,258.16 479.27,263.35 474.28,259.11 477.28,241.65 486.45,225.76 495.82,218.52 499.81,210.9 507.33,206.32 507.33,206.32z';
var wisc_river = 'M196.75,2.79 203.72,4.24 211.53,6.61 215.6,8.35 219.71,10.57 223.78,13.36 227.75,16.79 231.08,20.36 236.16,27.59 239.43,34.17 242.01,41.72 243.05,46.55 244.32,60.48 243.77,76.14 240.24,96.09 235.46,111.1 232.1,118.67 230.04,122.17 227.61,125.53 224.75,128.79 221.72,131.64 218.75,133.86 192.75,145.79 168.06,160.07 139.1,182.74 126.35,196.65 115.75,205.79 63.75,239.79 44.98,255.12 38.23,263.28 37.5,263.88 36.69,264.38 35.75,264.79 32.54,265.27 29.09,264.64 25.52,263.09 21.94,260.8 18.46,257.94 12.26,251.25 9.75,247.79 6.16,241.16 3.89,234.8 1.75,223.79 1,211.66 1.75,202.79 3.82,195.97 7.4,190.04 12.46,184.56 77.46,132.45 86.31,123.3 93.01,113.96 97.81,104.06 102.88,87.09 112.38,63.2 122.52,44.99 140.71,19.91 147.76,12.43 151.43,9.46 155.38,6.93 159.75,4.79 165.22,2.85 170.56,1.62 175.72,1 185.27,1.08 196.75,2.79 196.75,2.79z';
var driftless = 'M1,33.75 8.85,39.62 15.51,46.28 22.48,52.74 31.16,55.41 40.69,57.31 44.7,66.09 52.8,71.31 62.13,74.35 77.63,87.15 79.29,96.35 86.05,102.72 94,107.75 100.64,115.06 107.93,120.7 116.85,123.1 128.69,138.11 131.48,158.71 131.86,163.35 132.5,165.26 133.61,166.37 135.01,170.23 135.91,175.2 138.15,196.56 140.77,217.09 142,221.65 143.9,225.29 145.93,235.45 144.69,245.77 143.95,250.42 144.5,254.34 151.09,251.79 157.98,245.66 188.2,214.86 203.34,200.01 210.96,193.44 218.39,188.91 222.85,187.78 227.61,185.83 235.81,179.93 249.55,163.21 256.49,155.24 261.56,146.75 264.33,125.81 264.82,115.12 265.52,109.92 266.6,104.62 263.9,83.17 255.31,64.07 250.97,56.72 247.7,54.39 243.7,52.01 204.72,36.99 184.84,35.5 174.68,35.59 143.64,30.2 123.03,28.18 81.19,17.73 62.36,8.79 43.53,1 22.5,3.59 6.04,15.37 1,33.75 1,33.75z';
var coastal = "M11.1,333.95 1,309.32 9.42,286.44 27.77,271.43 38.45,248.2 26.38,205.24 26.93,183.32 34.12,163.2 23.19,141.76 22.57,117.21 50.47,82.59 58.64,81.84 58.24,90.13 61.14,90.92 70.68,84.49 80.29,77.91 84.87,67.79 96.6,53.11 121.7,24.32 126.56,16.04 129.54,12.87 133.7,9.8 135.1,5.95 137.77,3.46 146.16,1 147.12,6.23 146.1,9.95 144.65,10.1 143.1,9.95 142.81,11.71 142.46,13.37 140.06,15.15 139.89,17.96 138.75,21.92 135.81,24.37 134.85,28.85 127.74,43.55 123.88,52.82 118.37,62.37 101.67,102.22 98.18,113.51 99.88,123.9 101.37,131.61 98.95,136.06 94.09,138.33 88.28,145.79 87.92,149.36 86.09,153.82 82.13,175.52 84.76,193.73 76.08,212.71 75.25,218.4 72.24,223.23 69.39,234.02 68.95,256.96 69.83,267.76 69.1,272.95 71.86,277.79 72.31,287.54 75.97,296.63 78.1,299.95 78.25,304.77 76.32,313.43 75.22,324.05 75.79,332.42 67.1,333.08 56.13,332.86 32.35,333.64 11.1,333.95 11.1,333.95z";
var central_sands = "M9,92.55 5.14,86.91 2.76,81.61 1.5,77.28 1,74.55 1.43,62.88 5.1,52.58 10.47,43.76 16,36.55 31.82,20.88 47.49,10.62 60.16,4.82 67,2.55 73.92,1.19 86.8,1 147,9.55 204.71,22.9 224.26,29.79 241,37.55 252.63,44.09 261.15,50.04 267.34,55.74 272,61.55 280.95,80.97 283.33,98.98 282.29,112.52 281,118.55 273.79,134.67 264.51,146.47 255.48,154.31 249,158.55 243.44,161.36 232.19,165.4 216.6,168.01 198,166.55 180.14,159.24 149.15,135.37 124,122.55 102,115.55 87.77,112.62 47,107.55 30.07,103.75 19.22,99.92 12.76,96.16 9,92.55 9,92.55z";
var yahara = "M126.32,17.02 118.96,9.89 113.37,6.12 106.32,3.02 94.4,1 83.89,2.04 75.6,4.58 70.32,7.02 61.02,13.42 52.98,21.89 46.87,31.92 43.32,43.02 41.36,63.81 38.32,71.02 36.46,73.78 14.23,97.66 7.99,107.15 3.32,118.02 1.35,127.07 1,134.65 1.56,140.41 3.37,147.69 5.02,151.78 7.57,155.74 11.32,159.02 18.98,160.61 116.51,163.16 118.8,162.86 121.32,162.02 124.58,159.83 127.05,156.89 128.9,153.51 135.47,132.15 137.3,118.06 135.77,96.24 136.32,93.02 137.74,89.37 139.59,86.55 143.32,82.02 145.41,76.95 145.52,70.63 135.07,31.37 131.38,23.83 126.32,17.02 126.32,17.02z";
var north_woods = "M254.16,88.36 288.05,101.72 292.33,104.32 296.29,107.45 300.16,111.36 304.56,116.95 308.13,122.75 310.97,128.68 314.91,140.59 320.38,175.8 320.41,182.31 319.79,188.83 318.32,195.25 315.84,201.46 312.16,207.36 310.4,209.54 307.33,212.69 302.92,216.18 300.21,217.85 297.16,219.36 293.35,220.75 289.62,221.62 285.93,222.05 278.57,221.9 258.13,219.23 228.24,218.15 223.16,217.36 215.38,215.42 208.79,212.77 202.66,209.26 125.87,155.76 112.03,148.15 105.87,145.5 99.97,143.58 94.16,142.36 84.88,141.52 76.86,141.72 57.12,144.38 50.92,144.56 44.37,143.69 37.16,141.36 31.11,138.36 25.49,134.63 20.33,130.27 15.69,125.38 11.61,120.05 8.13,114.37 5.3,108.44 3.16,102.36 1,87.36 2.72,73.25 7.27,60.31 13.62,48.83 20.73,39.11 33.04,26.09 44.25,17.36 53.27,11.95 62.93,7.36 72.92,3.85 82.95,1.64 92.69,1 101.87,2.16 110.16,5.36 112.43,6.82 132.78,22.67 254.16,88.36 254.16,88.36z";
var nestedSquares = "M125,109 h60 v60 h-60z M125,171 h60 v60 h-60z M187,171 h60 v60 h-60z M63,47 h60 v60 h-60z M187,124 h14 v14 h-14z M187,233 h60 v60 h-60z M156,233 h29 v29 h-29z M156,264 h29 v29 h-29z M187,140 h29 v29 h-29z M94 ,109 h29 v29 h-29z M218,155 h14 v14 h-14z M109,140 h14 v14 h-14z M78 ,109 h14 v14 h-14z M140,233 h14 v14 h-14z M140,248 h14 v14 h-14z M140,264 h14 v14 h-14z M171,295 h14 v14 h-14z M187,295 h29 v29 h-29z M218,295 h29 v29 h-29z M125,78 h29 v29 h-29z M125,62 h14 v14 h-14z M140,62 h14 v14 h-14z M63,16 h29 v29 h-29z M94,31 h14 v14 h-14z M63,0 h14 v14 h-14z M47,31 h14 v14 h-14z M32,47 h29 v29 h-29z M47,78 h14 v14 h-14z M16,62 h14 v14 h-14z M0,47 h14 v14 h-14z M249,233 h14 v14 h-14z M249,249 h14 v14 h-14z M156,93 h14 v14 h-14z";
var stupidCTR = 1;
var f ="#000";
var dashDef = [6,4];

var chaff = "more descriptions and other whatnots and other bits and finally a bit more including special opportunities or risks for this area...";
var DSS_dummyData = [{
			   name: 'Urban',
			   data1: 25
			}, {
			   name: 'Forest',
			   data1: 25
			}, {
			   name: 'Row Crops',
			   data1: 25
			}, {
			   name: 'Grasses',
			   data1: 25
			}, {
			   name: 'Wetlands',
			   data1: 25
			}];

Ext.define('Ext.chart.theme.Muted2', {
    extend: 'Ext.chart.theme.Base',
    singleton: true,
    alias: [
        'chart.theme.muted2',
        'chart.theme.Muted2'
    ],
    config: {
        colors: [
            Ext.util.Color.fromString('#856585').createLighter().toHex(),
            Ext.util.Color.fromString('#915f44').createLighter().toHex(),
            Ext.util.Color.fromString('#a1a134').createLighter().toHex(),//4091ba
            Ext.util.Color.fromString('#7ca640').createLighter().toHex(),
            Ext.util.Color.fromString('#3b8d8b').createLighter().toHex(),
            Ext.util.Color.fromString('#8f8f8f').createLighter().toHex(),
            Ext.util.Color.fromString('#8f8f8f').createLighter().toHex(),
            Ext.util.Color.fromString('#6e8852').createLighter().toHex(),
            Ext.util.Color.fromString('#3dcc7e').createLighter().toHex(),
            Ext.util.Color.fromString('#a6bed1').createLighter().toHex(),
            Ext.util.Color.fromString('#cbaa4b').createLighter().toHex(),
            Ext.util.Color.fromString('#998baa').createLighter().toHex()
        ]
    }
});

Ext.define('SmartScapeDSS.view.Main', {
	extend: 'Ext.panel.Panel',
	
	layout: {
		type: 'vbox',
		align: 'middle'
	},
	minWidth: 960,
	minHeight: 768,
	scrollable: true,

	items: [{
		xtype: 'container',
		padding: 16,
		width: 350,
		html: '<a href="/alt"><img id="ddd" src="assets/images/dss_logo.png" style="width:100%"></a>',
		listeners: {
			afterrender: function(self) {
				Ext.defer(function() {
					self.updateLayout();
				}, 10);
			}	
		}
	
	},{
		xtype: 'container',
		layout: 'hbox',
		
		items: [{
			xtype: 'container',
			flex: 1
		},{
			xtype: 'container',
			layout: 'vbox',
			items: [{
				id: 'DSS_draw',
				xtype: 'draw',
				style: 'border-radius: 2px; border: 1px solid #bbb;',
				width: 540,
				height: 578,
				listeners: {
					element: 'element',
					scope: 'this',		
					mouseMove: 'mouseMove',
				},
				mouseMove: function(e) {
					var me = this,
					surface = me.getSurface(),
					hitResult = surface.hitTestEvent(e),
					oldSprite = me.oldSprite;
					
					if (oldSprite) {
						if (hitResult && hitResult.sprite && oldSprite === hitResult.sprite) {
						}
						else {
							Ext.get('pie_chart').setVisible(false);
							Ext.get('Title').setHtml('Choose an area of interest');
							Ext.get('Desc').setHtml(' ');
							var store = Ext.data.StoreManager.lookup('fake_pie');
							store.loadRawData(DSS_dummyData, false);
							oldSprite.setAnimation({
								duration: 250});
							oldSprite.setAttributes({
								/*strokeStyle: 'white',*/lineWidth: 2, opacity: 0.5//,lineDash:[4,4], lineDashOffset: 0
							});
						}
					}
					if (hitResult && hitResult.sprite && !hitResult.sprite.disableSelect) {
						Ext.get('pie_chart').setVisible(true);
						hitResult.sprite.setAnimation({
							duration: 1});
						hitResult.sprite.setAttributes({
							lineWidth: 3, opacity: 1//, lineDash: [8,0], lineDashOffset: 2
						});
						me.oldSprite = hitResult.sprite;
						Ext.get('Title').setHtml(hitResult.sprite.DSS_AreaTitle);
						Ext.get('Desc').setHtml(hitResult.sprite.DSS_AreaDescription + chaff);
						var store = Ext.data.StoreManager.lookup('fake_pie');
						store.loadRawData(hitResult.sprite.DSS_Data, false);
					}
					surface.renderFrame();		
				},
				
				sprites: [{
					type: 'image',
					src: 'assets/images/wisc_test_map.jpg',
					disableSelect: true,
					scaleX: 2, scaleY: 2
				},{
					type: 'path',
					path: wisc_river,
					fillStyle: 'rgba(50,100,150,0.75)',//'#1F6D91',
					stroke: 'white',lineWidth: 2,opacity: 0.5,lineDash: dashDef,
					translationX: 169,translationY: 275,
					DSS_AreaTitle: 'Wisconsin River Valley',
					DSS_AreaDescription: 'An area characterised by...',
					DSS_Data: [{
					   name: 'Urban',
					   data1: 10
					}, {
					   name: 'Forest',
					   data1: 30
					}, {
					   name: 'Row Crops',
					   data1: 31
					}, {
					   name: 'Grasses',
					   data1: 25
					}, {
					   name: 'Wetlands',
					   data1: 14
					}]
				},{
					type: 'path',
					path: driftless,
					fillStyle: 'rgba(90,150,30,0.75)',//'#1F6D91',
					stroke: 'white', lineWidth: 2, opacity: 0.5, lineDash: dashDef, lineDashOffset: 0,
					translationX: 27,translationY: 255,
					DSS_AreaTitle: 'The Driftless Area',
					DSS_AreaDescription: 'An area characterised by deep ravines and generally poor/shallow soils...',
					DSS_Data: [{
					   name: 'Urban',
					   data1: 5
					}, {
					   name: 'Forest',
					   data1: 30
					}, {
					   name: 'Row Crops',
					   data1: 35
					}, {
					   name: 'Grasses',
					   data1: 25
					}, {
					   name: 'Wetlands',
					   data1: 10
					}]
				},{
					type: 'path',
					path: coastal,
					fillStyle: 'rgba(30,170,180,0.75)',//'#1F6D91',
					stroke: 'white',lineWidth: 2,opacity: 0.5,lineDash: dashDef,
					translationX: 377,translationY: 222,
					DSS_AreaTitle: 'Coastal Lake Michigan',
					DSS_AreaDescription: 'An area characterised by...',
					DSS_Data: [{
					   name: 'Urban',
					   data1: 29
					}, {
					   name: 'Forest',
					   data1: 8
					}, {
					   name: 'Row Crops',
					   data1: 30
					}, {
					   name: 'Grasses',
					   data1: 30
					}, {
					   name: 'Wetlands',
					   data1: 22
					}]
				},{
					type: 'path',
					path: central_sands,
					fillStyle: 'rgba(180,180,90,0.75)',//'#1F6D91',
					stroke: 'white',lineWidth: 2,opacity: 0.5,lineDash: dashDef,
					translationX: 97,translationY: 175,
					DSS_AreaTitle: 'Central Sands',
					DSS_AreaDescription: 'An area characterised by...',
					DSS_Data: [{
					   name: 'Urban',
					   data1: 6
					}, {
					   name: 'Forest',
					   data1: 28
					}, {
					   name: 'Row Crops',
					   data1: 32
					}, {
					   name: 'Grasses',
					   data1: 20
					}, {
					   name: 'Wetlands',
					   data1: 6
					}]
				},{
					type: 'path',
					path: yahara,
					fillStyle: 'rgba(180,90,90,0.75)',//'#1F6D91',
					stroke: 'white',lineWidth: 2,opacity: 0.5,lineDash: dashDef,
					translationX: 272,translationY: 394,
					DSS_AreaTitle: 'Yahara Basin',
					DSS_AreaDescription: 'The drainage from the greater Madison area and ...',
					DSS_Data: [{
					   name: 'Urban',
					   data1: 30
					}, {
					   name: 'Forest',
					   data1: 20
					}, {
					   name: 'Row Crops',
					   data1: 32
					}, {
					   name: 'Grasses',
					   data1: 25
					}, {
					   name: 'Wetlands',
					   data1: 14
					}]
				},{
					type: 'path',
					path: north_woods,
					fillStyle: 'rgba(150,80,10,0.75)',//'#1F6D91',
					stroke: 'white',lineWidth: 2,opacity: 0.5,lineDash: dashDef,
					translationX: 80,translationY: 30,
					DSS_AreaTitle: 'Northern Woods',
					DSS_AreaDescription: 'An area characterised by sandier soils and ...',
					DSS_Data: [{
					   name: 'Urban',
					   data1: 6
					}, {
					   name: 'Forest',
					   data1: 40
					}, {
					   name: 'Row Crops',
					   data1: 15
					}, {
					   name: 'Grasses',
					   data1: 15
					}, {
					   name: 'Wetlands',
					   data1: 9
					}]
				}]
			},{
				xtype: 'container',
				width: 540,
				height: 32,
				style: 'font-size: 15px; color: #888; line-height: 22px; text-align: center;',
				html: 'Mouse over an area to select it'
			},{
				xtype: 'textareafield',
				id: 'dumpit',
				hidden: true,
				width: 200, height: 200,
				value: newStr
			}]
		},{
			xtype: 'container',
			layout: 'vbox',
			items: [{
				id: 'Title',
				xtype: 'container',
				width: 380,
				height: 32,
				margin: '0 4',
				style: 'padding: 5px; border: 1px solid #ccc; border-radius: 4px; font-size: 20px; line-height: 22px; font-weight: bold; text-align: center;',
				html: 'Choose an area of interest'
			},{
				id: 'Desc',
				xtype: 'container',
				margin: 4,
				style: 'padding: 8px; border: 1px solid #ccc; border-radius: 4px; font-size: 15px;',
				width: 380,
				height: 128
			},{
				id: 'pie_chart',
				xtype: 'polar',
				hideMode: 'offsets', // currently needed due to hidden chart + resize causing chart to lose its dimensions
				opacity: 0.5,
				width: 380,
				height: 350,
				innerPadding: 30,
				theme: 'muted2', // green, muted, midnight, sky, category1 - 6, blue
				store: {
					storeId: 'fake_pie',
					fields: ['name', 'data1'],
					data: DSS_dummyData
				},
				series: {
					type: 'pie',
					angleField: 'data1',
					totalAngle: Math.PI * 1.2,
					rotation: -Math.PI * 0.6,
					label: {
					   field: 'name',
					   labelOverflowPadding: 8,
					   display: 'outside',
					   calloutLine: {
						length: 48,
						width: 3
					   }
					},
					donut: 48
				},
				sprites: [{
					type: 'text',
					text: 'Landscape Characteristics',
					textAlign: 'center',
					translationX: 190,
					fontSize: 16,
					translationY: 230,
					fillStyle: '#777'
				}]
			}]
		},{
			xtype: 'container',
			flex: 1
		}]
	},{
		xtype: 'container',
		flex: 1
	},{
		xtype: 'container',
		height: 100,
		width: '100%',
		style: 'background-color: #ddd; border-top: 1px solid #ccc;',
		layout: {
			type: 'vbox',
			align: 'middle'
		},
		items: [{
			xtype: 'container',
			margin: 16,
			html: 'Footer text and more. And more and more and more.'
		},{
			xtype: 'container',
			flex: 1
		},{
			xtype: 'container',
			margin: 16,
			html: '&copy; 2019 wei.wisc.edu'
		}]
	}]
	
});

Ext.application({
    name: 'SmartScapeDSS',
    
    mainView: 'SmartScapeDSS.view.Main'
});


