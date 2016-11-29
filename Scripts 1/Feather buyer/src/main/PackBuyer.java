package main;

import java.awt.Graphics;

import gui.ScriptVars;
import gui.buyerGui;
import org.dreambot.api.data.GameState;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.friend.Friends;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.Quest;

import org.dreambot.api.methods.tabs.Tab;
import tools.PricedItem;
import tools.RunTimer;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Shop;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.impl.Condition;
import org.dreambot.api.wrappers.items.Item;




/*TO-DO:

- Cancel previous feather selling at GE and add newly bought feathers
- Git -> upload free
- edit GUI with other, more useful variables
- More random actions and msg responding
- Fix profit calculator (GE API from forum isn't working for me)
- Other future locations or shops to buy stuff from?

 */
@ScriptManifest(author = "Ulamog", name = "Intelligent Feather buyer/seller", version = 1.2, category = Category.MONEYMAKING, description = "Buys feather pack, opens it. Then collects and sells Feathers at GE and takes 500k and repeats. \nMakes use of random anti-ban actions and smart worldhops.\nSome credit goes to Nezz, for open sourcing a lot of code I used.")
public class PackBuyer extends AbstractScript{

	private boolean hopWorlds = false;
	private boolean walkToBank = false;
	private boolean bank = false;
    private boolean doGe = false;
    private boolean walkToShop = false;
	private final int[] f2pWorlds = new int[]{383,393,384,382,394};

	ScriptVars sv = new ScriptVars();

	PricedItem feathers;
	State state;
	RunTimer timer;
	boolean freeSlot;
	int tripCounter = 1;
	boolean started = false;

	private Area shopArea = new Area(new Tile(3011,3220,0), new Tile(3017,3229,0));
	private Area geArea = new Area(new Tile(3160,3494,0), new Tile(3172,3485,0));

	private enum State{
		BUY, OPEN_PACKS, HOP, WALK_TO_BANK, BANK, DO_GE, WALK_TO_SHOP
	}

	private State getState(){
		if(getInventory().contains(sv.packName)){
			return State.OPEN_PACKS;
		}
		if(hopWorlds)
			return State.HOP;
		if(walkToBank){
			return State.WALK_TO_BANK;
		}

		if(bank || (getTabs().isOpen(Tab.INVENTORY) && !getInventory().contains("Coins")) ){
            return State.BANK;
        }
        if(doGe){
            return State.DO_GE;
        }
        if(walkToShop){
            return State.WALK_TO_SHOP;
        }
		else
			return State.BUY;
	}

	public void onStart(){
		buyerGui gui = new buyerGui(sv);
		gui.setVisible(true);
		while(!sv.started){
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		String itemName = sv.packName.replace(" pack", "");
		log(itemName);
		feathers = new PricedItem(itemName, getClient().getMethodContext(), false);
		timer = new RunTimer();
		started = true;
        log("Feather price: " + feathers.getPrice());

	}

	@Override
	public int onLoop() {
		final Shop s = getShop();
		if(feathers == null){
			String itemName = sv.packName.replace(" pack", "");
			log(itemName);
			feathers = new PricedItem(itemName, getClient().getMethodContext(), false);
		}

		if(getPlayers().localPlayer().isMoving() && getClient().getDestination() != null && getClient().getDestination().distance(getPlayers().localPlayer().getTile()) > Calculations.random(3,11))
			return Calculations.random(200,300);

		state = getState();
		log("Loopstate: " + state);

		switch(state) {
			case HOP:
				if (sv.hopWorlds) {
					int hopTo = f2pWorlds[Calculations.random(0, f2pWorlds.length - 1)];
					while (hopTo == getClient().getCurrentWorld())
						hopTo = f2pWorlds[Calculations.random(0, f2pWorlds.length - 1)];

					getWorldHopper().hopWorld(hopTo);

					sleep(1800);
					if (!getTabs().isOpen(Tab.INVENTORY)) {
						log("Inventory tab not open");
						do {
							getTabs().open(Tab.INVENTORY);
							sleep(900);
							log("Opening inventory");
						} while (!getTabs().isOpen(Tab.INVENTORY));
						sleepUntil(() -> (getTabs().isOpen(Tab.INVENTORY)), 16000);
					}
					sleepUntil(() -> (shopArea.contains(getPlayers().localPlayer())), 16000);
					log("Hopped worlds and reading inventory Coins: " + getInventory().get("Coins").getAmount());
				} else {
					sleep(7000);
					log("world hopping is disabled");
					try {
						Thread.sleep(Calculations.random(10000, 20000));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				hopWorlds = false;
				break;

			case BUY:
				if (getTabs().isOpen(Tab.INVENTORY)){
					if (shopArea.contains(getPlayers().localPlayer())) {
						if (!s.isOpen() && getInventory().get("Coins").getAmount() > 300 && getInventory().contains("Coins")) {

							if (Calculations.random(1, 10) > 8) {
								if (Calculations.random(1, 100) > 77) {
									somethingRandom(Calculations.random(0, 11));
								} else if (Calculations.random(1, 4) == 4) {
									sleep(Calculations.random(50, 700));
								}
							}

							if (!getWalking().isRunEnabled() && getWalking().getRunEnergy() > Calculations.random(30, 70)) {
								getWalking().toggleRun();
							}
							s.open();
							waitFor(new Condition() {
								@Override
								public boolean verify() {
									return s.isOpen();
								}
							}, 1500);
						} else {
							if (getWorldHopper().isWorldHopperOpen()) {
								getWorldHopper().closeWorldHopper();
							}
							Item pack = s.get(sv.packName);
							if (pack != null && pack.getAmount() > sv.minAmt) {
								s.purchase(pack, 10);
								waitFor(new Condition() {
									public boolean verify() {
										return getInventory().contains(sv.packName);
									}
								}, 1000);
							} else {
								try {
									Thread.sleep(Calculations.random(200, 400));
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							pack = s.get(sv.packName);
							if (pack == null || pack.getAmount() < sv.minAmt && getInventory().get("Coins").getAmount() > 300)
								hopWorlds = true;
							if (s.isOpen()) {
								s.close();
							}
							if (getInventory().get("Coins").getAmount() < 300 || !getInventory().contains("Coins")) {
								walkToBank = true;
								if (s.isOpen()) {
									s.close();
								}
								log("Nut enuf cash, strarting next trip");
								if(tripCounter <= sv.Trips){
									tripCounter = tripCounter + 1;
								}
							}
						}
					} else if (getInventory().get("Coins").getAmount() <= 50000 || !getInventory().contains("Coins") && getTabs().isOpen(Tab.INVENTORY) && geArea.contains(getPlayers().localPlayer())) {
						bank = true;
					} else if ((getInventory().get("Coins").getAmount() <= 50000 || !getInventory().contains("Coins")) && getTabs().isOpen(Tab.INVENTORY) && (!shopArea.contains(getPlayers().localPlayer()) && getLocalPlayer().distance(new Tile(3013, 3224, 0)) <= 30 && !(getInventory().get("Coins").getAmount() < 300)) && !geArea.contains(getPlayers().localPlayer())) {
						log("Walking to bank because GP is" + getInventory().get("Coins").getAmount());
						walkToBank = true;
					} else if (!shopArea.contains(getPlayers().localPlayer())) {
						walkToShop = true;
					}
				}else{
						do {
								log("Buy -> open inventory first");
								sleep(1000);
								getTabs().openWithMouse(Tab.INVENTORY);


						}while(!getTabs().isOpen(Tab.INVENTORY));
				}
				break;

		case OPEN_PACKS:
			if(s.isOpen()){
				s.close();
			}
			else{
				for(int i = 0; i < 28; i++){
					Item it = getInventory().getItemInSlot(i);
					if(it != null && it.getName().equals(sv.packName)){
						getInventory().slotInteract(i, "Open");
						try {
							Thread.sleep(Calculations.random(100,150));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					feathers.update();
				}
				feathers.update();
			}
			break;
		case WALK_TO_BANK:
            if(!getInventory().contains("Coins")){
                log("Player has no coins");
            }
            if(!getWalking().isRunEnabled() && getWalking().getRunEnergy() > Calculations.random(30,70)){
                getWalking().toggleRun();
            }
			if(geArea.contains(getPlayers().localPlayer()) && tripCounter <= sv.Trips){
                walkToBank = false;
                log("Arrived at bank");
                bank = true;
			}else{
                getWalking().walk(BankLocation.GRAND_EXCHANGE.getCenter());
			}
			break;

        case BANK:
            if(getBank().isOpen()){

                getBank().depositAllItems();
                sleepUntil(new Condition(){
                    public boolean verify(){
                        return getInventory().isEmpty();
                    }
                },1000);

				if(getBank().contains("Coins")){
					getBank().withdraw("Coins", sv.GpPerTrip);
					sleepUntil(new Condition(){
						public boolean verify(){
							return getInventory().contains("Coins");
						}
					},1000);
				}

				if(getBank().contains("Feather")){
					getBank().withdrawAll("Feather");
					sleepUntil(new Condition(){
						public boolean verify(){
							return getInventory().contains("Feather");
						}
					},1000);
				}

				if(getInventory().count("Coins") < sv.GpPerTrip ){
                    log("Not enuf cash for trip");
					if(getInventory().contains("Feather")){
						doGe=true;
						bank=false;
					}else{
						getBank().close();
						stop();
					}
                }
                    getBank().close();
                    doGe = true;
                    bank = false;

            }
            else if(!geArea.contains(getPlayers().localPlayer())){
                bank = false;
                walkToBank = true;
            }
            else{
                getBank().open(BankLocation.GRAND_EXCHANGE);
                sleepUntil(new Condition(){
                    public boolean verify(){
                        return getBank().isOpen();
                    }
                },1200);
                log("Banking items except for Coins and Feather");
            }
            break;
		case DO_GE:
			if(getGrandExchange().isOpen()){

				if(getGrandExchange().collect()){
					sleep(Calculations.random(3000,6000));
				}


				if(getClient().isMembers()){
					for(int i=1; i<=8; i++){
						if(!getGrandExchange().slotContainsItem(i))
							freeSlot = true;
					}
				}else{
					for(int i=1; i<=3; i++){
						if(!getGrandExchange().slotContainsItem(i))
							freeSlot = true;
					}
				}


				if(getInventory().contains("Feather") && freeSlot){

					getGrandExchange().addSellItem("Feather");
					getGrandExchange().setPrice(3);

					getGrandExchange().confirm();

					sleepUntil(new Condition(){
						public boolean verify(){
							return getGrandExchange().isOpen() && !getGrandExchange().isSellOpen();
						}
					},1200);
				}else{
					log("not enough GE slots to sell feathers");
					stop();

				}
					freeSlot = false;
				getGrandExchange().close();
				sleepUntil(new Condition(){
					public boolean verify(){
						return !getGrandExchange().isOpen();
					}
				},2200);

				if(tripCounter > sv.Trips){
					stop();
				}

				if(!getInventory().contains("Feather") && (getInventory().count("Coins") >= 50000 && getInventory().count("Coins") == sv.GpPerTrip)){
					doGe = false;
					walkToShop = true;
					log("Start new trip");
				}
				else if(getInventory().contains("Feather")){
					doGe = true;
					log("Inventory still contains Feather");
				}
				else if(getInventory().count("Coins") != sv.GpPerTrip){
                    bank = true;
                    doGe = false;
					log("Trip amount is not in inventory");
                }

			}
			else{
				getGrandExchange().open();
				sleepUntil(new Condition(){
					public boolean verify(){
						return getGrandExchange().isOpen();
					}
				},1200);
				log("Collecting and selling Feather");
			}
			break;
		case WALK_TO_SHOP:
			if(shopArea.contains(getPlayers().localPlayer())){
				walkToShop = false;
			}
			else if(!shopArea.contains(getPlayers().localPlayer())){
				if(!getWalking().isRunEnabled() && getWalking().getRunEnergy() > Calculations.random(30,70)){
					getWalking().toggleRun();
				}
				getWalking().walk(shopArea.getCenter());
			}
			break;
		}
        return Calculations.random(100,200);
	}



	public void waitFor(Condition c, int timeout){
		long t = System.currentTimeMillis();
		while(System.currentTimeMillis() - t < timeout && !c.verify()){
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}



	public void somethingRandom(int a){      //static=?

		switch(a){
			case 0:
				getTabs().openWithMouse(Tab.QUEST);
				sleep(Calculations.random(600,1100));
				getEmotes().doRandomEmote();
				sleep(Calculations.random(800,1100));
				getTabs().open(Tab.INVENTORY);
				break;
			case 1:
				getTabs().openWithMouse(Tab.FRIENDS);
				sleep(Calculations.random(800,1100));
				getKeyboard().type("Pfff");
				sleep(Calculations.random(800,1100));
				getTabs().open(Tab.INVENTORY);
				break;
			case 2:
				sleep(Calculations.random(50,200));
				getTabs().open(Tab.PRAYER);
				getPrayer().toggleQuickPrayer(!getPrayer().isQuickPrayerActive());
				sleep(Calculations.random(100,300));
				getPrayer().toggleQuickPrayer(getPrayer().isQuickPrayerActive());
				sleep(Calculations.random(800,1100));
				getTabs().open(Tab.INVENTORY);
				break;
			case 3:
				getKeyboard().type(":)");
				sleep(Calculations.random(100,300));
				break;
			case 4:
				if(getClient().getLocalPlayer().distance(getPlayers().closest(player -> !player.isMoving() && !player.getName().equals(getLocalPlayer().getName()) )) <= 12){
					getKeyboard().type("2gp Feathers m8");
					sleep(Calculations.random(100,300));
				}
				break;
			case 5:
				getKeyboard().type(":/");
				sleep(Calculations.random(100,300));
				break;
			case 6:
				if(getClient().getLocalPlayer().distance(getPlayers().closest(player -> !player.isMoving() && !player.getName().equals(getLocalPlayer().getName()) )) <= 12)
					getKeyboard().type("Hi");
				sleep(Calculations.random(100,300));
				break;
			case 7:
				if(getClient().getLocalPlayer().distance(getPlayers().closest(player -> !player.isMoving() && !player.getName().equals(getLocalPlayer().getName()) )) <= 12)
					getKeyboard().type("Sup?");
				sleep(Calculations.random(100,300));
				break;
			case 8:
				if(getClient().getLocalPlayer().distance(getPlayers().closest(player -> !player.isMoving() && !player.getName().equals(getLocalPlayer().getName()) )) <= 12){
					getKeyboard().type("Hi " + getPlayers().closest(player -> !player.isMoving() && !player.getName().equals(getLocalPlayer().getName())).getName() );
					sleep(Calculations.random(500,1100));
				}
				break;
			case 9:
				if(getClient().getLocalPlayer().distance(getPlayers().closest(player -> !player.isMoving() && !player.getName().equals(getLocalPlayer().getName()) )) <= 12){
					getKeyboard().type("EZ $$$");
					sleep(Calculations.random(500,1100));
				}
				break;
			case 10:
				if(getClient().getLocalPlayer().distance(getPlayers().closest(player -> !player.isMoving() && !player.getName().equals(getLocalPlayer().getName()) )) <= 12){
					getKeyboard().type("...");
					sleep(Calculations.random(100,300));
				}
				break;
			case 11:
				if(getClient().getLocalPlayer().distance(getPlayers().closest(player -> !player.isMoving() && !player.getName().equals(getLocalPlayer().getName()) )) <= 12){
					String str = "";
					for(int i=1; i<Calculations.random(5,30); i++){
						str = str + "@";
					}
					getKeyboard().type(str);
					sleep(Calculations.random(100,300));
				}
				break;
		}


	}



	public void onPaint(Graphics g){
		if(started){
			if(state != null){
				g.drawString("State: " + state.toString(), 5, 50);
			}
			g.drawString(feathers.getName() + " bought(p/h): " + feathers.getAmount() + "(" + timer.getPerHour(feathers.getAmount()) +")", 5, 65);
			g.drawString("GP Made(p/h): " + (feathers.getAmount()*200/230) + "(" + timer.getPerHour(Math.round(feathers.getAmount()*200/230)) + ")", 5, 80);
			g.drawString("Time run: " + timer.format(),5, 95);



/*
            g.drawString(feathers.getName() + " bought(p/h): " + feathers.getAmount() + "(" + timer.getPerHour(feathers.getAmount()) +")", 5, 65);
            g.drawString("GP Made(p/h): " + feathers.getAmount()*(300 - sv.perItem) + "(" + timer.getPerHour(feathers.getAmount()*(300-sv.perItem)) + ")", 5, 80);
            g.drawString("Time run: " + timer.format(),5, 95);
            */

		}
	}




}

