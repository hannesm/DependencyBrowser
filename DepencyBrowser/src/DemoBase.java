import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import y.anim.AnimationFactory;
import y.anim.AnimationPlayer;
import y.base.Node;
import y.base.NodeCursor;
import y.layout.BufferedLayouter;
import y.layout.GraphLayout;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.LayoutMorpher;
import y.view.NavigationMode;


public class DemoBase extends Thread {
	  protected Graph2DView view;
	  private final JPanel contentPane;
	  private Layouter gr;
	  private final JToolBar jtb = new JToolBar();
	  private final JTextField search = new JTextField();
	  private LispMarshaller comm;
	  private JPopupMenu popup;
	  private JMenu extendmenu;

	public static void initLnF() {
		try {
			if ( !"com.sun.java.swing.plaf.motif.MotifLookAndFeel".equals(
					UIManager.getSystemLookAndFeelClassName()) &&
					!"com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(
							UIManager.getSystemLookAndFeelClassName()) &&
							!UIManager.getSystemLookAndFeelClassName().equals(
									UIManager.getLookAndFeel().getClass().getName() ) ) {
				UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
			}
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public DemoBase(Socket s) {
		comm = new LispMarshaller(s);
	    gr = new Layouter(this);
	    view = new Graph2DView();
	    view.setAntialiasedPainting( true );
	    view.addViewMode(new NavigationMode());
	    view.getCanvasComponent().addMouseWheelListener( new Graph2DViewMouseWheelZoomListener() );
	    view.getCanvasComponent().addMouseListener(new MyMouseListener());
	    view.setGraph2D(gr.graph);
	    contentPane = new JPanel();
	    contentPane.setLayout( new BorderLayout() );
	    contentPane.add(view, BorderLayout.CENTER);
	    search.getDocument().addDocumentListener(new SearchListener());
	    jtb.add(search);
	    contentPane.add(jtb, BorderLayout.NORTH);
	    popup = new JPopupMenu();
	    popup.add(new HideAction());
	    extendmenu = new JMenu("Extend");
	    popup.add(extendmenu);
	    popup.add(new ExploreAction());
	}
	
	  public final void run() {
		  String title = getTitle();
		  JFrame frame = new JFrame(title);
		  frame.getRootPane().setContentPane( contentPane );
		  frame.pack();
		  frame.setSize(1400, 1000);
		  frame.setLocationRelativeTo( null );
		  frame.setVisible( true );
		  receive("projects");
		  gr.getInitial();
	  }
	  
	  private String getTitle () {
		  	ArrayList request = new ArrayList();
		  	request.add(new Symbol("title"));
		  	comm.printMessage(request);
			ArrayList answer = comm.readMessage();
			assert(answer.size() == 2);
			assert(answer.get(0) instanceof Symbol);
			assert(answer.get(1) instanceof String);
			assert(((Symbol)answer.get(0)).isEqual("title"));
			return (String)answer.get(1);
	  }
	  
	  protected void receive (String r) {
		  receive(new Symbol(r));
	  }
	  
	  protected void receive (String name, Node n) {
		  receive(name, gr.text(n));
	  }
	  
	  protected void receive (String comm, String val) {
		  ArrayList a = new ArrayList();
		  a.add(new Symbol(comm));
		  a.add(val);
		  receive(a);
	  }
	  
	  protected void receive (Symbol r) {
		  ArrayList a = new ArrayList();
		  a.add(r);
		  receive(a);
	  }
	  
	  protected void receive (ArrayList request) {
			comm.printMessage(request);
			ArrayList answer = comm.readMessage();
			assert(answer.size() > 0);
			assert(answer.get(0) instanceof Symbol);
			Symbol key = (Symbol)answer.get(0);
			Commands.processCommand(key, answer, gr);
			this.relayout();
	  }
	  
	  protected void relayout () {
		  if (gr.change) {
			  Cursor oldCursor = view.getCanvasComponent().getCursor();
			  try {
				  //gr.organic.setNodeEdgeOverlapAvoided(true);
				  //gr.organic.setMinimalNodeDistance(10);
				  //gr.organic.setNodeSizeAware(true);
				  view.getCanvasComponent().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				  GraphLayout layout = new BufferedLayouter(gr.circular).calcLayout(view.getGraph2D());
				  LayoutMorpher morpher = new LayoutMorpher(view, layout);
				  final AnimationPlayer player = new AnimationPlayer();
				  player.addAnimationListener(view);
				  morpher.setSmoothViewTransform(true);
				  //morpher.setKeepZoomFactor(true);
				  morpher.setPreferredDuration(1500);
				  player.setFps(30);
				  //player.setBlocking(true);
				  player.animate(AnimationFactory.createEasedAnimation(morpher));
			  } catch (Exception e) {
				  e.printStackTrace();
			  } finally {
				  view.getCanvasComponent().setCursor(oldCursor);
				  gr.change = false;
			  }
		  }
	  }
	  
	  final class SearchListener implements DocumentListener {
		  private void search () {
			  String text = search.getText();
			  if (text.length() > 0)
				  gr.findSubString(text);
			  else
				  gr.graph.unselectNodes();
			  view.repaint();
		  }
		  
		public void changedUpdate(DocumentEvent e) {
		}

		public void insertUpdate(DocumentEvent e) {
			search();
		}

		public void removeUpdate(DocumentEvent e) {
			search();
		}
	  }
	  
	  final class MyMouseListener implements MouseListener {
		public void mouseClicked(MouseEvent arg0) {
		}
		
		public Node checkClick (Graph2DView graph, Graph2D g, int x, int y) {
			double xv = graph.toWorldCoordX(x);
			double yv = graph.toWorldCoordY(y);
			for (NodeCursor nc = g.nodes(); nc.ok(); nc.next())
				if (g.getRectangle(nc.node()).contains(xv, yv)) {
					return nc.node();
				}
			return null;
		}

		public void mouseEntered(MouseEvent arg0) {
		}

		public void mouseExited(MouseEvent arg0) {
		}

		public void mousePressed(MouseEvent arg0) {
			Node selected = checkClick(view, gr.graph, arg0.getX(), arg0.getY());
			if (selected != gr.getSelected()) {
				gr.unselect();
				if (selected != null) {
					gr.select(selected);
					maybePopup(arg0);
				}
			} else
				maybePopup(arg0);
		}

		public void mouseReleased(MouseEvent arg0) {
			mousePressed(arg0);
		}
		  
		private void maybePopup (MouseEvent arg0) {
	        if (arg0.isPopupTrigger()) {
	        	extendmenu.removeAll();
	        	assert(gr.getSelected() != null);
	        	for (String t : gr.getExtensions(gr.getSelected())) {
	        		JMenuItem mi = new JMenuItem(t);
	        		mi.addActionListener(new ExtendListener(t));
	        		extendmenu.add(mi);
	        	}
	            popup.show(arg0.getComponent(),
	                       arg0.getX(), arg0.getY());
	        }
		}
	  }
	  
	  final class ExtendListener implements ActionListener {
		  private String toof;
		  public ExtendListener (String s) {
			  toof = s;
		  }
		  
		public void actionPerformed(ActionEvent e) {
			gr.ensureNode(toof);
			relayout();
		}
	  }
	  
	  final class HideAction extends AbstractAction {
		  public HideAction () {
			  super("Hide Node");
		  }
		public void actionPerformed(ActionEvent e) {
			assert(gr.getSelected() != null);
			gr.unselectNeighbours();
			gr.hide(gr.getSelected());
			view.repaint();
		}
	  }
	  
	  final class ExploreAction extends AbstractAction {
		  public ExploreAction () {
			  super("Explore next level");
		  }
		  public void actionPerformed(ActionEvent e) {
			  assert(gr.getSelected() != null);
			  receive("explore", gr.getSelected());
		  }
	  }
}
