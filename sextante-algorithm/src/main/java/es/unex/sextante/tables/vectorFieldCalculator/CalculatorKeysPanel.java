package es.unex.sextante.tables.vectorFieldCalculator;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class CalculatorKeysPanel
         extends
            JPanel {

   private JTextArea m_TextExpression = null;
   private JButton   jButtonMinus;
   private JButton   jButtonDivide;
   private JButton   jButton2;
   private JButton   jButtonDot;
   private JButton   jButtonBrackets;
   private JButton   jButton0;
   private JButton   jButton9;
   private JButton   jButton8;
   private JButton   jButton7;
   private JButton   jButton6;
   private JButton   jButton5;
   private JButton   jButton4;
   private JButton   jButton3;
   private JButton   jButton1;
   private JButton   jButtonMultiply;
   private JButton   jButtonPlus;


   public CalculatorKeysPanel(final JTextArea textExpression) {

      super();

      m_TextExpression = textExpression;

      initialize();

   }


   private void initialize() {

      final ActionListener listener = new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            addText(evt.getSource());
         }
      };

      final ActionListener listenerBrackets = new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            m_TextExpression.insert(" ()", m_TextExpression.getCaretPosition());
            m_TextExpression.setCaretPosition(m_TextExpression.getCaretPosition() - 1);
         }
      };

      final TableLayout thisLayout = new TableLayout(
               new double[][] {
                        { TableLayoutConstants.FILL, 10.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL,
                                 TableLayoutConstants.FILL },
                        { TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL,
                                 TableLayoutConstants.FILL } });
      thisLayout.setHGap(5);
      thisLayout.setVGap(5);
      this.setLayout(thisLayout);
      this.setPreferredSize(new java.awt.Dimension(284, 239));
      {
         jButtonPlus = new JButton();
         this.add(jButtonPlus, "0, 0");
         jButtonPlus.setText("+");
         jButtonPlus.addActionListener(listener);
      }
      {
         jButtonMinus = new JButton();
         this.add(jButtonMinus, "0, 1");
         jButtonMinus.setText("-");
         jButtonMinus.addActionListener(listener);
      }
      {
         jButtonMultiply = new JButton();
         this.add(jButtonMultiply, "0, 2");
         jButtonMultiply.setText("*");
         jButtonMultiply.addActionListener(listener);
      }
      {
         jButtonDivide = new JButton();
         this.add(jButtonDivide, "0, 3");
         jButtonDivide.setText("/");
         jButtonDivide.addActionListener(listener);
      }
      {
         jButton1 = new JButton();
         this.add(jButton1, "2, 2");
         jButton1.setText("1");
         jButton1.addActionListener(listener);
      }
      {
         jButton2 = new JButton();
         this.add(jButton2, "3, 2");
         jButton2.setText("2");
         jButton2.addActionListener(listener);
      }
      {
         jButton3 = new JButton();
         this.add(jButton3, "4, 2");
         jButton3.setText("3");
         jButton3.addActionListener(listener);
      }
      {
         jButton4 = new JButton();
         this.add(jButton4, "2, 1");
         jButton4.setText("4");
         jButton4.addActionListener(listener);
      }
      {
         jButton5 = new JButton();
         this.add(jButton5, "3, 1");
         jButton5.setText("5");
         jButton5.addActionListener(listener);
      }
      {
         jButton6 = new JButton();
         this.add(jButton6, "4, 1");
         jButton6.setText("6");
         jButton6.addActionListener(listener);
      }
      {
         jButton7 = new JButton();
         this.add(jButton7, "2, 0");
         jButton7.setText("7");
         jButton7.addActionListener(listener);
      }
      {
         jButton8 = new JButton();
         this.add(jButton8, "3, 0");
         jButton8.setText("8");
         jButton8.addActionListener(listener);
      }
      {
         jButton9 = new JButton();
         this.add(jButton9, "4, 0");
         jButton9.setText("9");
         jButton9.addActionListener(listener);
      }
      {
         jButton0 = new JButton();
         this.add(jButton0, "2, 3");
         jButton0.setText("0");
         jButton0.addActionListener(listener);
      }
      {
         jButtonDot = new JButton();
         this.add(jButtonDot, "4, 3");
         jButtonDot.setText(".");
         jButtonDot.addActionListener(listener);
      }
      {
         jButtonBrackets = new JButton();
         this.add(jButtonBrackets, "3, 3");
         jButtonBrackets.setText("( )");
         jButtonBrackets.addActionListener(listenerBrackets);
      }
   }


   private void addText(final Object source) {

      if (source instanceof JButton) {
         String s = ((JButton) source).getText();
         try {
            final int i = Integer.parseInt(s);
         }
         catch (final NumberFormatException e) {
            s = " " + s + " ";
         }
         m_TextExpression.insert(s, m_TextExpression.getCaretPosition());
      }

   }

}
