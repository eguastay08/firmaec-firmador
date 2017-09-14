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
package ec.gob.firmadigital.lectorpdf;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.PageDrawer;
import org.apache.pdfbox.rendering.PageDrawerParameters;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

/**
 * Example showing custom rendering by subclassing PageDrawer.
 * 
 * <p>If you want to do custom graphics processing rather than Graphics2D rendering, then you should
 * subclass {@link PDFGraphicsStreamEngine} instead. Subclassing PageDrawer is only suitable for
 * cases where the goal is to render onto a Graphics2D surface.
 *
 * @author John Hewson
 * 
 * Modificada por Juan Diego Calle
 */
public class CustomPageDrawer
{
    
    public static PDFRenderer renderizar(PDDocument doc) throws IOException
    {
        /*File file = new File("src/main/resources/org/apache/pdfbox/examples/rendering/",
                             "custom-render-demo.pdf");*/

        System.out.println(doc.getNumberOfPages());
        PDFRenderer renderer = new MyPDFRenderer(doc);
        //renderer.renderImage.size();    
        //BufferedImage image = renderer.renderImage(0);
        //ImageIO.write(image, "PNG", new File("/tmp/custom-render.png"));
        return renderer;

    }

    /**
     * Example PDFRenderer subclass, uses MyPageDrawer for custom rendering.
     */
    private static class MyPDFRenderer extends PDFRenderer
    {
        MyPDFRenderer(PDDocument document)
        {
            super(document);
        }

        @Override
        protected PageDrawer createPageDrawer(PageDrawerParameters parameters) throws IOException
        {
            return new MyPageDrawer(parameters);
        }
    }

    /**
     * Example PageDrawer subclass with custom rendering.
     */
    private static class MyPageDrawer extends PageDrawer
    {
        MyPageDrawer(PageDrawerParameters parameters) throws IOException
        {
            super(parameters);
        }

        /**
         * Color replacement.
         */
        @Override
        protected Paint getPaint(PDColor color) throws IOException
        {
            // if this is the non-stroking color
            if (getGraphicsState().getNonStrokingColor() == color)
            {
                // find red, ignoring alpha channel
                if (color.toRGB() == (Color.RED.getRGB() & 0x00FFFFFF))
                {
                    // replace it with blue
                    return Color.BLUE;
                }
            }
            return super.getPaint(color);
        }

        /**
         * Glyph bounding boxes.
         */
        @Override
        protected void showGlyph(Matrix textRenderingMatrix, PDFont font, int code, String unicode,
                                 Vector displacement) throws IOException
        {
            // draw glyph
            super.showGlyph(textRenderingMatrix, font, code, unicode, displacement);
            
            // bbox in EM -> user units
            Shape bbox = new Rectangle2D.Float(0, 0, font.getWidth(code) / 1000, 1);
            AffineTransform at = textRenderingMatrix.createAffineTransform();
            bbox = at.createTransformedShape(bbox);
            
            // save
            Graphics2D graphics = getGraphics();
            Color color = graphics.getColor();
            Stroke stroke = graphics.getStroke();
            Shape clip = graphics.getClip();

            // draw
            //graphics.setClip(graphics.getDeviceConfiguration().getBounds());
            //graphics.setColor(Color.RED);
            //graphics.setStroke(new BasicStroke(.5f));
            //graphics.draw(bbox);

            // restore
            graphics.setStroke(stroke);
            graphics.setColor(color);
            graphics.setClip(clip);
        }

        /**
         * Filled path bounding boxes.
         */
        @Override
        public void fillPath(int windingRule) throws IOException
        {
            // bbox in user units
            Shape bbox = getLinePath().getBounds2D();
            
            // draw path (note that getLinePath() is now reset)
            super.fillPath(windingRule);
            
            // save
            Graphics2D graphics = getGraphics();
            Color color = graphics.getColor();
            Stroke stroke = graphics.getStroke();
            Shape clip = graphics.getClip();

            // draw
            graphics.setClip(graphics.getDeviceConfiguration().getBounds());
            graphics.setColor(Color.GREEN);
            graphics.setStroke(new BasicStroke(.5f));
            graphics.draw(bbox);

            // restore
            graphics.setStroke(stroke);
            graphics.setColor(color);
            graphics.setClip(clip);
        }

        /**
         * Custom annotation rendering.
         */
        @Override
        public void showAnnotation(PDAnnotation annotation) throws IOException
        {
            // save
            saveGraphicsState();
            
            // 35% alpha
            getGraphicsState().setNonStrokeAlphaConstant(0.35);
            super.showAnnotation(annotation);
            
            // restore
            restoreGraphicsState();
        }
    }
}
