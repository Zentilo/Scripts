import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.LinkedList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;

import org.powerbot.concurrent.Task;
import org.powerbot.concurrent.strategy.Condition;
import org.powerbot.concurrent.strategy.Strategy;
import org.powerbot.game.api.ActiveScript;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Tabs;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.Locations;
import org.powerbot.game.api.methods.node.Menu;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.tab.Skills;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.wrappers.interactive.Player;
import org.powerbot.game.api.wrappers.node.Item;
import org.powerbot.game.api.wrappers.node.Location;
import org.powerbot.game.api.wrappers.widget.WidgetChild;
import org.powerbot.game.bot.event.MessageEvent;
import org.powerbot.game.bot.event.listener.MessageListener;
import org.powerbot.game.bot.event.listener.PaintListener;





@Manifest(
		name = "Lunar Planker",
		version = 2.01,
		description = "Uses Plank Make Spell - By xScott and DontPayForFilms, Start with Logs + Planks in at top of bank",
		authors = {             "xScott", "DontPayForFilms"        }
		)
public class LunarPlanker extends ActiveScript implements PaintListener, MessageListener, MouseListener, MouseMotionListener
{
	private final static int[] BankIDsLocs =
		{
		2738,   /*Soulwars chest*/                      25808,  /*Seers village bank booths*/
		2213,   /*Catherby/Yanille*/            782,    /*Varrock west/east bankbooth*/
		36786,  /*Lumb bankbooth*/                      11758,  /*Falador booth (W/E)*/
		4483,   /*Castle wars chest*/           27663,  /*Duel areana chest*/
		42192,  /*Burthope(N) Chest*/           66665,  /*Burthrope(S) Table*/
		35647,  /*Al-Kharid Booth*/                     2012,   /*Draynor booth*/
		42373,  /*Edgevile booth*/                      34752,  /*Ardy(N/S)*/  
		16700, /*Moonclan booth*/                       2738    /*soulwars*/
		};

	//GUI IDs
	public int LOG_ID = 0;
	int TeakId = 6333;
	int mahogId = 6332;
	int OakId = 1521;
	int NormId = 1511;
	int PLANK_ID = 0;

	//Variables
	private static int BankID;
	private static boolean CheckedSpellBook = false;
	private static String Status = "Starting up";
	private static int startExp;
	private static long startTime;
	private static boolean GUIDone = false;

	//Widgets
	private static WidgetChild PlankMakeSpell;
	private static WidgetChild[] InventoryItems;
	private static WidgetChild CloseBank;
	private static WidgetChild MagicStats;
	private static WidgetChild LogoutCross;
	private static WidgetChild LogoutButton;

	private final RenderingHints antialiasing = new RenderingHints(
			RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	//private final Color color2 = new Color(0, 102, 51);
	private final Color color3 = new Color(255, 255, 255);

	private final BasicStroke stroke1 = new BasicStroke(1);
	 private final LinkedList<MousePathPoint> mousePath = new LinkedList<MousePathPoint>();

	private final Font font1 = new Font("Arial", 0, 12);
	private final Font font2 = new Font("Arial", 1, 16);

	@Override
	protected void setup()
	{  

		InitiateGui();
		InventoryItems = Widgets.get(679,0).getChildren();
		PlankMakeSpell = Widgets.get(430, 33);
		CloseBank = Widgets.get(762, 45);
		MagicStats = Widgets.get(320, 66);
		LogoutCross = Widgets.get(548, 150);
		LogoutButton = Widgets.get(182,7);
		startExp = Skills.getExperiences()[6];
		startTime = System.currentTimeMillis();
		
		LocateNearestBanker();
		
		if(validSpellBookLayout() == false)
		{
			Logout();
			stop();
		}      
		Tabs.INVENTORY.open();
		provide(new MakePlanks());
		provide(new Bank());
		provide(new AntiBan());
	}

	private class Bank extends Strategy implements Task, Condition
	{


		public boolean withdraw(int id) {
			WidgetChild[] bank = Widgets.get(762, 95).getChildren();
			for (WidgetChild child : bank) {
				if (child.getChildId() == id) {
					Mouse.click(child.getRelativeX() + 40,
							child.getRelativeY() + 105, false);
					return Menu.select("Withdraw-All");
				}
			}
			return false;
		}

		private void Deposit(int id)
		{
			if(InventoryItems[24] != null && InventoryItems[24].getChildId() == id)
			{

				InventoryItems[24].interact("Deposit-All");    
			}
			else
			{
				for(Item I : Inventory.getItems())
				{
					if(I.getId() == id)
					{
						I.getWidgetChild().interact("Deposit-All");
						break;
					}                  
				}
			}
		}

		@Override
		public void run()
		{
			Location bank_ = Locations.getNearest(BankID);
			Status = "Interacting with bank...";
			if(!bank_.interact("Use")) { bank_.interact("Bank"); }
			Time.sleep(500);
			Status = "Depositing";
			Deposit(PLANK_ID);
			Time.sleep(600);
			Status = "Withdrawing";
			withdraw(LOG_ID);
			Time.sleep(Random.nextInt(100,200));
			CloseBank.click(true);
			Tabs.INVENTORY.open();
		}

		@Override
		public boolean validate()
		{
			for(Item I : Inventory.getItems())
			{
				if(I.getId() == LOG_ID)
				{
					return false;
				}
			}
			return true;
		}
	}

	private class MakePlanks extends Strategy implements Task, Condition
	{
		private void CheckMagicTabIsOpen()
		{
			if(!Tabs.MAGIC.open())  {       Tabs.MAGIC.open();      }
		}

		@Override
		public void run()
		{
			Status = "Checking...";
			CheckMagicTabIsOpen();
			Status = "Casting spell";
			for(Item I : Inventory.getItems())
			{
				if(I.getId() == LOG_ID)
				{
					PlankMakeSpell.click(true);
					I.getWidgetChild().click(true);
					Time.sleep(Random.nextInt(1100, 1300));
					break;
				}
			}
		}

		@Override
		public boolean validate()
		{
			for(Item I : Inventory.getItems())
			{
				if(I.getId() == LOG_ID)
				{
					return true;
				}
			}
			return false;
		}
	}

	private class AntiBan extends Strategy implements Task, Condition
	{

		@Override
		public void run()
		{
			Antiban(Random.nextInt(0,160));

		}

		@Override
		public boolean validate()
		{
			return true;
		}

		void Antiban(int randomNumber)
		{
			if(randomNumber % 2 == 0)
			{
				Status = "Anti-ban";
				switch(randomNumber)
				{
				case 0:
				case 2:
				case 4:
				case 6:
				case 8:
				{
					Camera.setAngle(Random.nextInt(43, 311));
					break;
				}
				case 10:
				{
					for(Player P : Players.getLoaded())
					{
						if(P.isOnScreen())
						{
							P.click(false);
							Time.sleep(Random.nextInt(400,700));
							break;
						}
					}
				}
				case 14:
				{
					Tabs.STATS.open();
					MagicStats.hover();
					Time.sleep(700);
					break;
				}
				case 16:
				{
					Time.sleep(Random.nextInt(500, 2100));
					break;
				}
				}
			}

		}

	}

	private boolean LocateNearestBanker()
	{
		Status = "Locating nearest banker...";

		for(Location Loc : Locations.getLoaded())
		{
			for(final int I : BankIDsLocs)
			{
				if(Loc.getId() == I)
				{
					if(Loc.isOnScreen())
					{
						BankID = I;
						return true;
					}
				}
			}
		}
		return false;
	}

	private String formatTime(final long milliseconds)
	{
		final long t_seconds = milliseconds / 1000;
		final long t_minutes = t_seconds / 60;
		final long t_hours = t_minutes / 60;
		final long seconds = t_seconds % 60;
		final long minutes = t_minutes % 60;
		final long hours = t_hours % 500;
		return hours + ":" + minutes + ":" + seconds;
	}

	private void Logout()
	{
		Time.sleep(500);
		LogoutCross.click(true);
		Time.sleep(500);
		LogoutButton.click(true);
	}

	private boolean validSpellBookLayout()
	{
		if(!PlankMakeSpell.getChildName().contains("Plank"))
			return false;
		else
			return true;
	}

	@Override
	public void onRepaint(Graphics g1)
	{

		int ExpGained = (Skills.getExperiences()[6] - startExp);
		int ExpPerHour = (int)Math.floor((ExpGained * 3600000D) / (System.currentTimeMillis() - startTime) / 1000);
		String exphr = ExpPerHour + "k";
		String timeElapsed = formatTime(System.currentTimeMillis() - startTime);

		Graphics2D g = (Graphics2D)g1;
		g.setRenderingHints(antialiasing);

		g.setColor(new Color(0,0,0,120));
		g.fillRoundRect(4, 227, 229, 109, 16, 16);
		g.setColor(Color.BLACK);
		g.setStroke(stroke1);
		g.drawRoundRect(4, 227, 229, 109, 16, 16);
		g.setFont(font1);
		g.setColor(color3);
		g.drawString("Time elapsed: " + timeElapsed, 22, 266);
		g.drawString("Experience gained: " + ExpGained, 22, 283);
		g.drawString("Exp/hr: " + exphr, 22, 301);
		g.drawString("Status: " + Status, 22, 319);
		g.setFont(font2);
		g.setColor(Color.GRAY);
		g.drawString("xScotts Lunar Planker ", 37, 248);

		g.setColor(Color.ORANGE);
		g.fillOval(Mouse.getX(), Mouse.getY(), 7, 7);
		g.setColor(Color.RED);
		g.fillOval(Mouse.getX() + 1, Mouse.getY() + 1, 5, 5);
		
		 while (!mousePath.isEmpty() && mousePath.peek().isUp())
             mousePath.remove();
Point clientCursor = Mouse.getLocation();
MousePathPoint mpp = new MousePathPoint(clientCursor.x, clientCursor.y,
                             1000); //1000 = lasting time/MS
if (mousePath.isEmpty() || !mousePath.getLast().equals(mpp))
             mousePath.add(mpp);
MousePathPoint lastPoint = null;
for (MousePathPoint a : mousePath) {
             if (lastPoint != null) {
                             g.setColor(Color.BLACK);
                             g.drawLine(a.x, a.y, lastPoint.x, lastPoint.y);
             }
             lastPoint = a;
}

	}

	
	@Override
	public void messageReceived(MessageEvent arg0)
	{
		if(arg0.getMessage().contains("You do not have enough astral")) {               Logout();       stop();         }
		else if(arg0.getMessage().contains("You do not have enough nature")) {          Logout();       stop();         }
		else if(arg0.getMessage().contains("You do not have enough earth")) {           Logout();       stop();         }
		else if(arg0.getMessage().contains("You need")) {               Logout();       stop();         }

	}

	
	@SuppressWarnings("serial")
    private class MousePathPoint extends Point { // All credits to Enfilade
                    
                    private long finishTime;
                    private double lastingTime;

                    private int toColor(double d) {
                                    return Math.min(255, Math.max(0, (int) d));
                    }

                    public MousePathPoint(int x, int y, int lastingTime) {
                                    super(x, y);
                                    this.lastingTime = lastingTime;
                                    finishTime = System.currentTimeMillis() + lastingTime;
                    }

                    public boolean isUp() {
                                    return System.currentTimeMillis() > finishTime;
                    }
    }

	
	public void InitiateGui() 
	{
		SwingUtilities.invokeLater(new Runnable() 
		{
			public void run() 
			{
				final Gui Lunars = new Gui();
				Lunars.setVisible(true);
			}
		});
		final waitGui guiTask = new waitGui();
		provide(new Strategy(guiTask, guiTask));
	}
	
	private final class waitGui implements Condition, Task {      

		public void run() 
		{
			while(!GUIDone)
			{
				Time.sleep(15);
			}
		}

		public boolean validate() 
		{
			return !GUIDone;
		}
	}

	
	@SuppressWarnings("serial")
	private final class Gui extends JFrame implements ActionListener {
		public Gui() {
			initComponents();
		}

		private void button1ActionPerformed(ActionEvent e) {
			String PlankToMake = comboBox1.getSelectedItem().toString();

			if(PlankToMake.equals("Mahogany")) {
				LOG_ID = mahogId;
				PLANK_ID = 8782;
			} if(PlankToMake.equals("Teak")) {
				LOG_ID = TeakId;
				PLANK_ID = 8780;
			} if(PlankToMake.equals("Oak")) {
				LOG_ID = OakId;
				PLANK_ID = 8778;
			} if(PlankToMake.equals("Normal")) {
				LOG_ID = NormId;
				PLANK_ID = 960;
			}
			this.setVisible(false);
			this.dispose();
			GUIDone = true;
		}



		private void initComponents() {
			button1 = new JButton();
			comboBox1 = new JComboBox();
			checkBox1 = new JCheckBox();

			//======== this ========
					setTitle("Menu");
					Container contentPane = getContentPane();

					//---- button1 ----
					button1.setText("Start");
					button1.setEnabled(true);
					button1.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button1ActionPerformed(e);
						}
					});

					//---- comboBox1 ----
					comboBox1.setEnabled(true);
					comboBox1.setModel(new DefaultComboBoxModel(new String[] {
							"Mahogany",
							"Teak",
							"Oak",
							"Normal",
					}));

					GroupLayout contentPaneLayout = new GroupLayout(contentPane);
					contentPane.setLayout(contentPaneLayout);
					contentPaneLayout.setHorizontalGroup(
							contentPaneLayout.createParallelGroup()
							.addGroup(contentPaneLayout.createSequentialGroup()
									.addContainerGap()
									.addGroup(contentPaneLayout.createParallelGroup()
											.addComponent(comboBox1, GroupLayout.Alignment.TRAILING, 0, 182, Short.MAX_VALUE)
											.addComponent(button1, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE))
											.addContainerGap())
							);
					contentPaneLayout.setVerticalGroup(
							contentPaneLayout.createParallelGroup()
							.addGroup(contentPaneLayout.createSequentialGroup()
									.addContainerGap()
									.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(comboBox1, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
									.addComponent(button1)
									.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
							);
					pack();
					setLocationRelativeTo(getOwner());
		}

		private JButton button1;
		private JComboBox comboBox1;
		private JCheckBox checkBox1;

		@Override
		public void actionPerformed(ActionEvent arg0) {

		}
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		Point p = arg0.getPoint();

		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}