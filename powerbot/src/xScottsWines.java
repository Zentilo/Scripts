import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import org.powerbot.concurrent.Task;
import org.powerbot.concurrent.strategy.Condition;
import org.powerbot.concurrent.strategy.Strategy;
import org.powerbot.game.api.ActiveScript;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Tabs;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.input.Keyboard;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.Locations;
import org.powerbot.game.api.methods.node.Menu;
import org.powerbot.game.api.methods.tab.Inventory;
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
		name = "xScottsWines",
		version = 1.00,
		description = "Runs at any bank; have both Grapes and Jugs of wine visible in bank.",
		authors = {             "xScott"        }
		)
public class xScottsWines extends ActiveScript implements PaintListener, MessageListener
{
	//Bank locations
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

	//Variables
	private static int BankID;
	private static int WinesMade = 0;
	private static long startTime;
	private final static int Grapes = 1987;
	//private final static int JugOfWine = 1995;
	private final static int JugOfWater = 1937;
	private static boolean MadeAllWines = false;
	private static String Status = "Starting";

	//Widgets
	private static WidgetChild CloseBank;
	private static WidgetChild DepositAll;
	private static WidgetChild MakeAllWines;
	private static WidgetChild CookStats;

	//Paint attributes
	private final RenderingHints antialiasing = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	private final Color color3 = new Color(255, 255, 255);
	private final BasicStroke stroke1 = new BasicStroke(1);
	private final Font font1 = new Font("Arial", 0, 12);
	private final Font font2 = new Font("Arial", 1, 16);


	@Override
	protected void setup() 
	{

		CloseBank = Widgets.get(762, 45);
		DepositAll = Widgets.get(762, 34);
		MakeAllWines = Widgets.get(905, 14);
		CookStats = Widgets.get(320, 47);

		startTime = System.currentTimeMillis();

		LocateNearestBanker();

		provide( new Bank() );
		provide( new MakeWines() );
		provide( new AntiBan() );
	}


	private class MakeWines extends Strategy implements Condition, Task
	{
		WidgetChild invGrape;
		WidgetChild invJugOfWater;

		@Override
		public void run() 
		{
			invGrape = GetWidgetFromInventory(Grapes);
			invJugOfWater = GetWidgetFromInventory(JugOfWater);

			invGrape.click(true);
			invJugOfWater.click(true);
			Time.sleep(1150);

			if(MakeAllWines.isVisible() == true)
			{
				MakeAllWines.click(true);
			}
			else
			{
				Time.sleep(100);
				run();
			}


			Locations.getNearest(BankID).hover();//Hover over bank for faster banking.

			Status = "Waiting for wines to be made.";
			while(MadeAllWines == false)
			{
				if(!HasItem(Grapes))
				{
					MadeAllWines = true;
				}
			}
			MadeAllWines = false;
		}

		@Override
		public boolean validate()
		{
			if(HasItem(Grapes) && HasItem(JugOfWater))
			{
				return true;
			}
			else
			{
				return false;
			}

		}

	}

	private class Bank extends Strategy implements Condition, Task
	{

		//Helborn's bank function i believe.
		private boolean withdrawBankItem(final int id, final int amount) 
		{
			final WidgetChild bankSlotsContainer = Widgets.get(762, 95);
			for (final WidgetChild bankSlot : bankSlotsContainer.getChildren()) 
			{
				if (bankSlot != null && bankSlot.getChildId() == id) 
				{
					final int bankSlotX = bankSlotsContainer.getRelativeX()
							+ bankSlot.getRelativeX();
					final int bankSlotY = bankSlotsContainer.getRelativeY()
							+ bankSlot.getRelativeY();
					if (Mouse.click(bankSlotX + (bankSlot.getWidth() / 2),
							bankSlotY + (bankSlot.getHeight() / 2), false)) {
						if (amount == 0) {
							return Menu.isOpen() && Menu.select("Withdraw-All");
						} else {
							if (Menu.contains("Withdraw-" + amount)) {
								return Menu.isOpen()
										&& Menu.select("Withdraw-" + amount);
							} else {
								if (!Menu.select("Withdraw-x")) {
									return false;
								}
								Time.sleep(Random.nextInt(1400, 1900));
								Keyboard.sendText(Integer.toString(amount), true);
								return true;
							}
						}
					}
					return false;
				}
			}
			return false;
		}


		@Override
		public void run() 
		{
			Location bank_ = Locations.getNearest(BankID);
			Status = "Interacting with bank...";
			if(!bank_.interact("Use")) { bank_.interact("Bank"); }
			Time.sleep(1500);

			Status = "Depositing";
			DepositAll.click(true);
			Time.sleep(550);

			Status = "Withdrawing";
			withdrawBankItem(Grapes, 14);
			Time.sleep(450);
			withdrawBankItem(JugOfWater, 14);
			Time.sleep(Random.nextInt(100,200));

			if(HasItem(Grapes) == false && HasItem(JugOfWater) == false)	{	run();	}
			else if(!(HasItem(Grapes) == false || HasItem(JugOfWater) == false))	{	run();	}

			CloseBank.click(true);
			Tabs.INVENTORY.open();

		}

		@Override
		public boolean validate()
		{
			if(HasItem(Grapes) && HasItem(JugOfWater))
			{
				return false;
			}
			else
			{
				return true;
			}
		}

	}
	
	private class AntiBan extends Strategy implements Task, Condition
	{

		@Override
		public void run()
		{
			Antiban(Random.nextInt(0,50));

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
					CookStats.hover();
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

	private WidgetChild GetWidgetFromInventory(int ID)
	{
		for(Item I : Inventory.getItems())
		{
			if(I.getId() == ID)
			{
				return I.getWidgetChild();
			}
		}
		return Widgets.get(0,0);
	}
	private boolean HasItem(int ID)
	{
		for(Item I : Inventory.getItems())
		{
			if(I.getId() == ID)
			{
				return true;
			}
		}
		return false;
	}
	//IDK whos method this is, please post and credit will be given.
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

	@Override
	public void onRepaint(Graphics g1) 
	{
		int ExpGained = WinesMade * 200;
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
		g.drawString("xScotts Wines! ", 57, 248);

		g.setColor(Color.ORANGE);
		g.fillOval(Mouse.getX() - 3, Mouse.getY() - 3, 7, 7);
		g.setColor(Color.RED);
		g.fillOval(Mouse.getX() -2, Mouse.getY()-2, 5, 5);

	}

	@Override
	public void messageReceived(MessageEvent arg0)
	{
		if(arg0.getMessage().contains("You squeeze"))
		{
			WinesMade++;
		}

	}



}
