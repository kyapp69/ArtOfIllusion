/* Copyright (C) 1999-2006 by Peter Eastman

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.ui;

import artofillusion.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;

/** A ToolPalette is drawn as a grid of images, one for each EditingTool that is added to
    the palette.  It allows a single tool to be selected at any time. */

public class ToolPalette extends CustomWidget
{
  private int width, height, numTools, selected, lastSelected, defaultTool;
  private EditingTool tool[];
  private Dimension maxsize;

  /** Create a new ToolPalette.  w and h give the width and height of the tool palette,
      measured in icons. */

  public ToolPalette(int w, int h)
  {
    width = w;
    height = h;
    tool = new EditingTool [w*h];
    numTools = 0;
    selected = 0;
    defaultTool = 0;
    maxsize = new Dimension(0, 0);
    addEventLink(MousePressedEvent.class, this, "mousePressed");
    addEventLink(MouseClickedEvent.class, this, "mouseClicked");
    addEventLink(RepaintEvent.class, this, "paint");
    addEventLink(ToolTipEvent.class, this, "showToolTip");
    setBackground(ModellingApp.APP_BACKGROUND_COLOR);
  }

  /** Add a new tool. */

  public void addTool(EditingTool t)
  {
    addTool(numTools, t);
  }

  /** Add a new tool. */

  public void addTool(int position, EditingTool t)
  {
    if (numTools == tool.length)
    {
      // We need to extend the palette.

      height++;
      EditingTool newTool[] = new EditingTool [width*height];
      System.arraycopy(tool, 0, newTool, 0, tool.length);
      tool = newTool;
      invalidateSize();
    }
    for (int i = numTools; i > position; i--)
      tool[i] = tool[i-1];
    tool[position] = t;
    numTools++;
    int w = t.getIcon().getWidth(null);
    int h = t.getIcon().getHeight(null);
    if (w > maxsize.width)
      maxsize.width = w;
    if (h > maxsize.height)
      maxsize.height = h;
    if (numTools == 1)
      t.activate();
  }

  /** Get the number of tools in palette. */

  public int getNumTools()
  {
    return numTools;
  }

  /** Get a tool by index. */

  public EditingTool getTool(int index)
  {
    return tool[index];
  }

  /** Get the default tool. */

  public EditingTool getDefaultTool()
  {
    return tool[defaultTool];
  }

  /** Set the default tool. */

  public void setDefaultTool(EditingTool t)
  {
    for (int i = 0; i < tool.length; i++)
      if (tool[i] == t)
        defaultTool = i;
  }

  /** Return the number of the currently selected tool. */

  public int getSelection()
  {
    return selected;
  }

  /** Return the currently selected tool. */

  public EditingTool getSelectedTool()
  {
    return tool[selected];
  }

  private void paint(RepaintEvent ev)
  {
    Graphics g = ev.getGraphics();
    for (int i = 0; i < numTools; i++)
    {
      if (i == selected)
        g.drawImage(tool[i].getSelectedIcon(), (i%width)*maxsize.width, (i/width)*maxsize.height, null);
      else
        g.drawImage(tool[i].getIcon(), (i%width)*maxsize.width, (i/width)*maxsize.height, null);
    }
    g.drawLine(0, 0, width*maxsize.width, 0);
    g.drawLine(0, height*maxsize.height, width*maxsize.width, height*maxsize.height);
  }

  private void showToolTip(ToolTipEvent ev)
  {
    int i = findClickedTool(ev.getPoint());
    if (i < numTools)
    {
      String text = tool[i].getToolTipText();
      if (text == null)
        BToolTip.hide();
      else
        new BToolTip(text).processEvent(ev);
    }
  }

  public Dimension getPreferredSize()
  {
    return new Dimension(width*maxsize.width, height*maxsize.height+2);
  }

  public Dimension getMinimumSize()
  {
    return getPreferredSize();
  }

  private void mousePressed(MousePressedEvent e)
  {
    int i = findClickedTool(e.getPoint());
    if (i < numTools && i != selected)
    {
      if (selected < tool.length)
        tool[selected].deactivate();
      selected = lastSelected = i;
      repaint();
      tool[i].activate();
    }
  }

  private void mouseClicked(MouseClickedEvent e)
  {
    int i = findClickedTool(e.getPoint());
    if (i < numTools && e.getClickCount() == 2)
      tool[i].iconDoubleClicked();
  }

  private int findClickedTool(Point p)
  {
    return (p.x/maxsize.width) + ((p.y-1)/maxsize.height)*width;
  }

  /** Change the currently selected tool. */

  public void selectTool(EditingTool which)
  {
    selectToolInternal(which);
    lastSelected = selected;
  }

  /** This is used internally to actually change the selected tool. */

  private void selectToolInternal(EditingTool which)
  {
    for (int i = 0; i < numTools; i++)
      if (tool[i] == which)
      {
        tool[selected].deactivate();
        selected = i;
        repaint();
        tool[i].activate();
      }
  }

  /** Allow the user to change tools with the keyboard. */

  public void keyPressed(KeyPressedEvent ev)
  {
    int code = ev.getKeyCode();
    int newtool;

    if (code == KeyPressedEvent.VK_LEFT)
      newtool = selected-1;
    else if (code == KeyPressedEvent.VK_RIGHT)
      newtool = selected+1;
    else if (code == KeyPressedEvent.VK_UP)
      newtool = selected-width;
    else if (code == KeyPressedEvent.VK_DOWN)
      newtool = selected+width;
    else
      return;
    if (newtool < 0)
      newtool += numTools;
    if (newtool >= numTools)
      newtool -= numTools;
    tool[selected].deactivate();
    selected = lastSelected = newtool;
    repaint();
    tool[selected].activate();
  }

  /** Calling this method will toggle between the default tool and the last tool which was
      explicitly selected. */

  public void toggleDefaultTool()
  {
    selectToolInternal(tool[selected == lastSelected ? defaultTool : lastSelected]);
  }
}