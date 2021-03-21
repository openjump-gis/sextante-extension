/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI for
 * visualizing and manipulating spatial features with geometry and attributes.
 * 
 * Copyright (C) 2003 Vivid Solutions
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * For more information, contact:
 * 
 * Vivid Solutions Suite #1A 2328 Government Street Victoria BC V8T 5G5 Canada
 * 
 * (250)385-6040 www.vividsolutions.com
 */
package es.unex.sextante.gui.toolbox;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;

import es.unex.sextante.core.ObjectAndDescription;
import es.unex.sextante.dataObjects.IDataObject;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;


@SuppressWarnings("rawtypes")
public class ObjectAndDescriptionRenderer extends JPanel implements ListCellRenderer,
TreeCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// <<TODO>> See how the colour looks with other L&F's. [Jon Aquino]




	GridBagLayout gridBagLayout = new GridBagLayout();

	protected JLabel label = new JLabel();





	private DefaultListCellRenderer defaultListCellRenderer = new DefaultListCellRenderer();




	private JLabel imageLabel = new JLabel();
	private ImageIcon rasterIcon = new ImageIcon(getClass().getClassLoader().getResource("images/Image.png"));
	private ImageIcon polygonIcon = new ImageIcon(getClass().getClassLoader().getResource("images/Polygon.png"));
	private ImageIcon lineStringIcon = new ImageIcon(getClass().getClassLoader().getResource("images/LineString.png"));
	private ImageIcon pointIcon = new ImageIcon(getClass().getClassLoader().getResource("images/Point.png"));

	private ImageIcon table_Icon = new ImageIcon(getClass().getClassLoader().getResource("images/table_icon.png"));

	public ObjectAndDescriptionRenderer() {
		super();
		setOpaque(true);
		setName("List.layerNameRenderer");

		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}



	public JLabel getLabel() {
		return label;
	}


	/**
	 * @param i
	 *            zero-based
	 */
	protected int getColumnWidth(int i) {
		validate();
		return gridBagLayout.getLayoutDimensions()[0][i];
	}

	protected int getRowHeight() {
		validate();
		return gridBagLayout.getLayoutDimensions()[1][0];
	}



	/**
	 * Workaround for bug 4238829 in the Java bug database: "JComboBox
	 * containing JPanel fails to display selected item at creation time"
	 */
	@Override
	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, y, w, h);
		validate();
	}

	/**
	 * Special getListCellRendererComponent to render simple Strings. It is not
	 * the normal use, but it makes it possible to pass special values as
	 * "All Layers" or "Selected Layers" (used in QueryDialog). [mmichaud
	 * 2011-09-27]
	 */
	public Component getListCellRendererComponent(JList list, String value,
			int index, boolean isSelected, boolean cellHasFocus) {
		label.setText(value);
		imageLabel.setVisible(false);

		if (isSelected) {
			setForeground(list.getSelectionForeground());
			setBackground(list.getSelectionBackground());
		} else {
			setForeground(list.getForeground());
			setBackground(list.getBackground());
		}
		return this;
	}

	private Component formatLayerEntry(JList list, Object value, int index,
			boolean isSelected, boolean cellHasFocus) {
		// only treat layers & strings
		if (value == null
				|| !(value instanceof IDataObject || value instanceof String))
			return defaultListCellRenderer.getListCellRendererComponent(list,
					value, index, isSelected, cellHasFocus);

		// Accepting String is not the normal use, but it makes it possible
		// to pass special values as "All Layers" or "Selected Layers" (used in
		// QueryDialog).
		if (value instanceof String) {
			return getListCellRendererComponent(list, (String) value, index,
					isSelected, cellHasFocus);
		}

		// assign layername to list entry
		ObjectAndDescription objectAndDescription = (ObjectAndDescription) value;

		final String description = objectAndDescription.getDescription();
		 
		final Object layer = objectAndDescription.getObject();
		
		label.setText(description);

		label.setVisible(true);

		if (isSelected) {
			Color sbg = list.getSelectionBackground();
			Color sfg = list.getSelectionForeground();
			setBackground(sbg);
			setForeground(sfg);
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}






		if (layer instanceof IRasterLayer) {
			// switch icon accoring to contained image count
			imageLabel.setIcon(rasterIcon);
			imageLabel.setVisible(true);
		} else if (layer instanceof ITable) {
			imageLabel.setIcon(table_Icon);
			imageLabel.setVisible(true);
		} else if (layer instanceof IVectorLayer) {
			if(((IVectorLayer) layer).getShapeType() ==0) {
				imageLabel.setIcon(pointIcon);
				imageLabel.setVisible(true);
			}else
				if(((IVectorLayer) layer).getShapeType() ==1) {
					imageLabel.setIcon(lineStringIcon);
					imageLabel.setVisible(true);
				}
				else
					if(((IVectorLayer) layer).getShapeType() ==2) {
						imageLabel.setIcon(polygonIcon);
						imageLabel.setVisible(true);
					}
		}


		return this;
	}

	private JList list(JTree tree) {
		JList list = new JList();
		list.setForeground(tree.getForeground());
		list.setBackground(tree.getBackground());
		list.setSelectionForeground(UIManager
				.getColor("Tree.selectionForeground"));
		list.setSelectionBackground(UIManager
				.getColor("Tree.selectionBackground"));
		return list;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		// generally format layer
		formatLayerEntry(list, value, index, isSelected, cellHasFocus);

		// assign proper width to cell entry
		// setPreferredSize(getPreferredListCellSize());

		return this;
	}

	// helper method to assign fg/bgcolor to _all_ panel components at once
	private void _setComponentsFBGColor(Color c, boolean fg) {
		for (Component comp : getComponents()) {
			if (fg)
				comp.setForeground(c);
			else
				comp.setBackground(c);
		}
	}

	@Override
	public void setForeground(Color c) {
		super.setForeground(c);
		_setComponentsFBGColor(c, true);
	}

	@Override
	public void setBackground(Color c) {
		super.setBackground(c);
		_setComponentsFBGColor(c, false);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		IDataObject layerable = (IDataObject) value;
		// generally format layer
		formatLayerEntry(list(tree), layerable, row, selected, hasFocus);
		// assign proper width to cell entry
		// setPreferredSize(getPreferredListCellSize());
		if (selected) {
			label.setForeground(UIManager.getColor("Tree.selectionForeground"));
			label.setBackground(UIManager.getColor("Tree.selectionBackground"));
			setForeground(UIManager.getColor("Tree.selectionForeground"));
			setBackground(UIManager.getColor("Tree.selectionBackground"));
		} else {
			label.setForeground(tree.getForeground());
			label.setBackground(tree.getBackground());
			setForeground(tree.getForeground());
			setBackground(tree.getBackground());
		}


		return this;
	}

	void jbInit() throws Exception {
		//this.setLayout(gridBagLayout);
		this.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));
		this.add(imageLabel);

		this.add(label);
		// label.setOpaque(false);
		/*	label.setText("None");

		Insets space_insets = new Insets(0, 0, 0, 0);
		this.add(imageLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				space_insets, 0, 0));

		this.add(label, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE,
				space_insets, 0, 0));*/
	}



	/*
	 * Associate Byte, Megabytes, etc to file
	 */
	private static final String[] Q = new String[] { "", "KB", "MB", "GB",
			"TB", "PB", "EB" };

	/*
	 * Return bytres as string
	 */
	public String getAsString(long bytes) {
		for (int i = 6; i > 0; i--) {
			double step = Math.pow(1024, i);
			if (bytes > step)
				return String.format("%3.1f %s", bytes / step, Q[i]);
		}
		return Long.toString(bytes);
	}




	@Override
	// [ede 11.2012] this is necessary for comboboxes with transparent bg, like
	// in
	// default vista/win7 lnf, else ugly background is painted behind the
	// letters
	public boolean isOpaque() {
		Color bgc = getBackground();
		Component p;
		// fetch cellrendererpane's parent if possible
		if ((p = getParent()) != null)
			p = p.getParent();
		// calculate our opaque state by honoring our parents values
		boolean colorMatchOrOpaque = (bgc != null) && (p != null)
				&& bgc.equals(p.getBackground()) && p.isOpaque();
		return !colorMatchOrOpaque && super.isOpaque();
	}


}