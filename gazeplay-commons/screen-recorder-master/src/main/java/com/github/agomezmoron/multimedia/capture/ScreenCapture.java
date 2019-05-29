/**
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Alejandro Gómez Morón
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.agomezmoron.multimedia.capture;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;

/**
 * It models a capture from the screen.
 * 
 * @author Alejandro Gomez <agommor@gmail.com>
 *
 */
public class ScreenCapture implements Serializable{

    /**
	 * Auto generated serial version UID
	 */
	private static final long serialVersionUID = 2843139292448505412L;
	
	/**
     * Capture in a {@link BufferedImage} instance.
     */
    private BufferedImage source;

    /**
     * Constructor.
     * @param source {@link BufferedImage} instance.
     */
    public ScreenCapture(BufferedImage source) {
        this.setSource(source);
    }

    /**
     * @return the source.
     */
    public BufferedImage getSource() {
        return source;
    }

    /**
     * @param source the source to set.
     */
    protected void setSource(BufferedImage source) {
        this.source = source;
    }

    /**
     *  @return the height of the capture.
     */
    public int getHeight() {
        int height = 0;
        if (this.source != null) {
            height = this.source.getHeight();
        }
        return height;
    }

    /**
     *  @return the width of the capture.
     */
    public int getWidth() {
        int width = 0;
        if (this.source != null) {
            width = this.source.getWidth();
        }
        return width;
    }
    
    /**
     * Custom serializable write function
     * @param o
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream o)
    	    throws IOException { 
    	
    	//Converts the buffered image in a byte array
    	byte[] imageInByte;
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	ImageIO.write(this.source, "jpg", baos);
    	baos.flush();
        imageInByte = baos.toByteArray();
        baos.close();
    	
        o.write(imageInByte);
    }
    
    /**
     * Custom serializable read function
     * @param o
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream o)
    	    throws IOException, ClassNotFoundException {  
    	
    	 //Converts the byte array in a buffered image
        InputStream in = new ByteArrayInputStream((byte[]) o.readObject());
        this.source = ImageIO.read(in);
    	
    }
}
