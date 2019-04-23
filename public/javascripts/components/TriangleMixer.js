
//------------------------------------------------------------------------------
Ext.define('DSS.components.TriangleMixer', {
//------------------------------------------------------------------------------
	extend: 'Ext.container.Container',
	alias: 'widget.triangle_mixer',
	
	width: 300,
	height: 260,
	layout: 'fit',

	initComponent: function() {
		var me = this;
		
		Ext.applyIf(me, {
			items: [{
				xtype: 'draw',
				bodyStyle: 'background: transparent',
				border: false,
				sprites: [{
					type: 'path',
					path: 'M0,208 240,208 L120,0 Z',
					fillStyle: 'rgba(32,48,255,0.1)',
					strokeStyle: 'rgba(0,0,0,0.4)',
					translation: {x: 30, y: 26},
					shadowColor: '#000',
					shadowBlur: 8,
					shadowOffsetY: -4
				},{
					type: 'text',
					text: 'Economics',
					x: 110, y: 16,
					fontSize: 16,
					fillStyle: '#444'
				},{
					type: 'text',
					text: 'Ecology',
					x: 5, y: 254,
					fontSize: 16,
					fillStyle: '#444'
				},{
					type: 'text',
					text: 'Emissions',
					x: 225, y: 254,
					fontSize: 16,
					fillStyle: '#444'
				},{
					type: 'circle',
					cx: 0, cy: 0,
					translate: {x:120 + 30,y:139 + 26},
					r: 12,
					fillStyle: 'rgb(71,110,156)',
					strokeStyle: '#fff',
					lineWidth: 2,
					shadowColor: 'rgba(0,0,0,0.5)',
					shadowBlur: 16,
					shadowOffsetY: 12
				}]
			}]
		});
		
		me.callParent(arguments);
	}
	
});

