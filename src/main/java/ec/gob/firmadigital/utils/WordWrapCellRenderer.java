/* 
 * Copyright (C) 2017 FirmaEC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ec.gob.firmadigital.utils;

import java.awt.Color;
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
        Color colorOriginal = new Color(214,217,223,100);
        Color colorFocus = new Color(160,162,172,100);
        
        
        if(hasFocus){
            //System.out.println("esta seleccionado "+ this.getBackground());
            this.setBackground(colorFocus);
            
        }else{
            this.setBackground(colorOriginal);
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
