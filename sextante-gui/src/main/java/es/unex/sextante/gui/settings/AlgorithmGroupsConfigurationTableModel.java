package es.unex.sextante.gui.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.toolbox.AlgorithmGroupConfiguration;

public class AlgorithmGroupsConfigurationTableModel
         extends
            AbstractTableModel {

   private final Object[][]                         m_Data;
   private final String[]                           COLUMN_NAMES;
   private final AlgorithmGroupsConfigurationDialog m_Parent;


   public AlgorithmGroupsConfigurationTableModel(final HashMap<String, AlgorithmGroupConfiguration> confMap,
                                                 final AlgorithmGroupsConfigurationDialog parent) {


      m_Parent = parent;

      COLUMN_NAMES = new String[] { Sextante.getText("AlgName"), Sextante.getText("AlgCmdName"), Sextante.getText("AlgGroup"),
               Sextante.getText("AlgSubgroup"), Sextante.getText("ShowAlgorithm") };

      final ArrayList<Object> data = new ArrayList<Object>();
      final HashMap<String, HashMap<String, GeoAlgorithm>> map = Sextante.getAlgorithms();

      final Set<String> set = map.keySet();
      final Iterator<String> iter = set.iterator();
      while (iter.hasNext()) {
         final String sKey = iter.next();
         final HashMap<String, GeoAlgorithm> group = map.get(sKey);
         final Set<String> set2 = group.keySet();
         final Iterator<String> iter2 = set2.iterator();
         while (iter2.hasNext()) {
            final String sKey2 = iter2.next();
            final GeoAlgorithm alg = group.get(sKey2);
            final AlgorithmGroupConfiguration conf = confMap.get(alg.getCommandLineName());
            if (conf == null) {
               data.add(new Object[] { alg.getCommandLineName(), alg.getName(), sKey, alg.getGroup(), Boolean.TRUE });
            }
            else {
               data.add(new Object[] { alg.getCommandLineName(), alg.getName(), conf.getGroup(), conf.getSubgroup(),
                        new Boolean(conf.isShow()) });
            }
         }
      }

      m_Data = new Object[data.size()][5];
      for (int j = 0; j < m_Data.length; j++) {
         m_Data[j] = (Object[]) data.get(j);
      }

   }


   public AlgorithmGroupsConfigurationTableModel(final AlgorithmGroupsConfigurationDialog parent) {

      super();

      m_Parent = parent;

      COLUMN_NAMES = new String[] { Sextante.getText("AlgCmdName"), Sextante.getText("AlgName"), Sextante.getText("AlgGroup"),
               Sextante.getText("AlgSubgroup"), Sextante.getText("ShowAlgorithm") };

      final ArrayList<Object> data = new ArrayList<Object>();
      final HashMap<String, HashMap<String, GeoAlgorithm>> map = Sextante.getAlgorithms();

      final Set<String> set = map.keySet();
      final Iterator<String> iter = set.iterator();
      while (iter.hasNext()) {
         final String sKey = iter.next();
         final HashMap<String, GeoAlgorithm> group = map.get(sKey);
         final Set<String> set2 = group.keySet();
         final Iterator<String> iter2 = set2.iterator();
         while (iter2.hasNext()) {
            final String sKey2 = iter2.next();
            final GeoAlgorithm alg = group.get(sKey2);
            data.add(new Object[] { alg.getCommandLineName(), alg.getName(), sKey, alg.getGroup(), Boolean.TRUE });
         }
      }

      m_Data = new Object[data.size()][5];
      for (int j = 0; j < m_Data.length; j++) {
         m_Data[j] = (Object[]) data.get(j);
      }


   }


   public int getColumnCount() {


      return 5;

   }


   public int getRowCount() {

      return m_Data.length;

   }


   public Object getValueAt(final int rowIndex,
                            final int columnIndex) {

      return m_Data[rowIndex][columnIndex];

   }


   @Override
   public String getColumnName(final int iCol) {

      return COLUMN_NAMES[iCol];

   }


   @Override
   public Class getColumnClass(final int c) {

      return getValueAt(0, c).getClass();

   }


   @Override
   public boolean isCellEditable(final int iRow,
                                 final int iCol) {

      return (iCol != 0) && (iCol != 1);

   }


   @Override
   public void setValueAt(final Object value,
                          final int row,
                          final int col) {

      m_Data[row][col] = value;
      m_Parent.hasBeenModified();
      fireTableCellUpdated(row, col);

   }


}
