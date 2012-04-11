import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.RenderingHints;

import org.powerbot.concurrent.Task;
import org.powerbot.concurrent.strategy.Condition;
import org.powerbot.concurrent.strategy.Strategy;
import org.powerbot.game.api.ActiveScript;
import org.powerbot.game.api.methods.Tabs;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.input.Keyboard;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.node.Locations;
import org.powerbot.game.api.methods.node.Menu;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.wrappers.node.Location;
import org.powerbot.game.api.wrappers.widget.WidgetChild;
import org.powerbot.game.bot.event.MessageEvent;
import org.powerbot.game.bot.event.listener.MessageListener;
import org.powerbot.game.bot.event.listener.PaintListener;


public class xScottsHerbCleaner extends ActiveScript implements MessageListener, PaintListener
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

	//Variables
	private static int BankID;
	private static int WinesMade = 0;
	private static long startTime;
	private static final int Grimy = 0;
	private static final int Clean = 0;
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
	public void onRepaint(Graphics arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected void setup() 
	{
		// TODO Auto-generated method stub
		
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
			Time.sleep(Random.nextInt(100,200));


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
	@Override
	public void messageReceived(MessageEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}



	@Override
	public void messageReceived(MessageEvent arg0) {
		// TODO Auto-generated method stub
		
	}
