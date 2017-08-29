/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.firmadigital.utils;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author jdc
 */
public class WordWrapCellRenderer extends JTextArea implements TableCellRenderer {
    public WordWrapCellRenderer(){
        setLineWrap(true);
        setWrapStyleWord(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        if(value != null){
            setText(value.toString());
        }else{
            setText("");
        }
      /*  if(column == 2){
            System.out.println("Alto: " + getPreferredSize().height+ " " +table.getRowHeight(row));
        }*/
        setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);
        if(table.getRowHeight(row) < getPreferredSize().height){
            table.setRowHeight(row,getPreferredSize().height);
        }
        return this;
    }
}
