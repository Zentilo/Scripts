import java.awt.Container;
import java.awt.Point;
import java.awt.RenderingHints.Key;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;

import org.powerbot.concurrent.Task;
import org.powerbot.concurrent.strategy.Condition;
import org.powerbot.concurrent.strategy.Strategy;
import org.powerbot.game.api.ActiveScript;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Tabs;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.input.Keyboard;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.node.Locations;
import org.powerbot.game.api.methods.node.Menu;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.wrappers.node.Item;
import org.powerbot.game.api.wrappers.node.Location;
import org.powerbot.game.api.wrappers.widget.WidgetChild;




@Manifest(
		name = "Superheater",
		version = 1.0,
		description = "Superheater...",
		authors = { "null" }
		)
public class Superheater extends ActiveScript 
{

	private final static int[] PrimaryOres = 
		{
		440, //Iron ore
		436, //Copper
		444 //Gold
		};
	/*private final static int[] SecondaryOres = 
		{
		453, //Coal ore
		438, //Tin ore
		};*/

	private static WidgetChild CloseBank;
	private static WidgetChild SuperheatSpell;
	private static boolean GUIDone = false;
	private static String BarsHeating;
	private static int PrimaryOreID; //GET THE ID FROM THE GUI!	
	private static int SecondaryOreID;
	





	@Override
	protected void setup() 
	{
		
		InitiateGui();
		
		SuperheatSpell = Widgets.get(192, 50);
		CloseBank = Widgets.get(762, 45);

		provide( new Superheat() );
		provide( new Bank() );

	}

	private class Superheat extends Strategy implements Task, Condition
	{
		private void CheckMagicTabIsOpen()
		{
			if(!Tabs.MAGIC.open())	{	Tabs.MAGIC.open();	}
		}


		@Override
		public void run()
		{
			CheckMagicTabIsOpen();	//Check if magic tab is open (Fail-safes).
			SuperheatSpell.click(true);	//Click on the Cast superheat spell.


			//Look for a primary ore in the inventory and click on it (Spell has already been selected).
			for(Item I : Inventory.getItems())
			{
				if(I.getId() == PrimaryOreID)
				{
					I.getWidgetChild().click(true);
					Time.sleep(Random.nextInt(800,900));
					break;
				}
			}
		}

		@Override
		public boolean validate()
		{
			for(Item I : Inventory.getItems())
			{
				for(int x : PrimaryOres)
				{
					if(I.getId() == x)
					{
						return true;
					}
				}
			}
			return false;

		}

	}










	private class Bank extends Strategy implements Task, Condition
	{

		
		private void WithdrawOresForBars(String bar)
		{
			switch(bar)
			{
			case "Steel":
				withdraw(440, 9); //Iron
				withdraw(453, 0); //Coal
				break;
			case "Iron":
				withdraw(440, 0);
				break;
			case "Bronze":
				withdraw(436, 13);
				withdraw(438 ,13);
				break;
			case "Gold":
				withdraw(444, 0);
				break;
			case "mith":
				break;
			}
		}

		
		private boolean withdraw(final int id, final int amount) 
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

		private void Deposit()
		{
			for(Item I : Inventory.getItems())
			{
				if(I.getName().contains("bar"))
				{
					I.getWidgetChild().interact("Deposit-All");
					break;
				}                  
			}
			
		}
		@Override
		public void run()
		{
			Location bank_ = Locations.getNearest(2738);

			if(!bank_.interact("Use")) { bank_.interact("Bank"); }
			Time.sleep(500);

			Deposit();
			Time.sleep(600);

			WithdrawOresForBars(BarsHeating);
			Time.sleep(Random.nextInt(100,200));
			
			CloseBank.click(true);
			Time.sleep(600);
			
			Tabs.INVENTORY.open();
			// TODO Auto-generated method stub

		}

		@Override
		public boolean validate()
		{
			for(Item I : Inventory.getItems())
			{
				if(I.getId() == PrimaryOreID)
				{
					return false;
				}
			}
			return true;
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
			while(GUIDone == false)
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
	private final class Gui extends JFrame implements ActionListener 
	{
		public Gui()
		{
			initComponents();
		}

		private void button1ActionPerformed(ActionEvent e) {
			String PlankToMake = comboBox1.getSelectedItem().toString();

			if(PlankToMake.equals("Bronze")) {
				PrimaryOreID = 436;
				//SecondaryOreID = 438;
				BarsHeating = "Bronze";
					
			} if(PlankToMake.equals("Iron")) {
				PrimaryOreID = 440;
				//BarsHeating = "Iron";

			} if(PlankToMake.equals("Steel")) {
				PrimaryOreID = 440;
				//SecondaryOreID = 453;
				BarsHeating = "Steel";

			} if(PlankToMake.equals("Gold")) {
				PrimaryOreID = 444;
				BarsHeating = "Gold";
			}
			
			this.setVisible(false);
			this.dispose();
			GUIDone = true;
		}



		private void initComponents() 
		{
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
							"Bronze",
							"Iron",
							"Steel",
							"Gold",
							"Mithril",
							"Adamantine",
							"Runite"
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
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
}


