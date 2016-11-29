package tools;

import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.methods.MethodProvider;
import org.dreambot.api.script.AbstractScript;

import static tools.GrandExchangeApi.*;

public class PricedItem {
	private String name;
	private int lastCount = 0;
	private int amount = 0;
	private int price = 0;
	private int id = 0;
	private MethodContext ctx;

	public PricedItem(String name, MethodContext ctx, boolean getPrice){
		this.name = name;
		this.ctx = ctx;
		if(ctx.getInventory().contains(name)){
			lastCount = (int) ctx.getInventory().count(name);
		}
		if(getPrice){
			MethodProvider.log("Getting price");

			GrandExchangeApi exchangeApi = new GrandExchangeApi();
			GELookupResult lookupResult = exchangeApi.lookup(314);				//314 for Feather
			if(lookupResult != null){
				price = lookupResult.price;
				//price = PriceGrab.getInstance().getPrice(tempName, 2);
				MethodProvider.log("Got price: " + price);
			}
		}
		else
			price = 0;
	}

	public PricedItem(String name, int id , MethodContext ctx, boolean getPrice){
		this.name = name;
		this.ctx = ctx;
		this.setId(id);
		if(ctx.getInventory().contains(id))
			lastCount = (int) ctx.getInventory().count(id);
		if(getPrice) {
			GrandExchangeApi exchangeApi = new GrandExchangeApi();
			GELookupResult lookupResult = exchangeApi.lookup(314);                //314 for Feather
			if (lookupResult != null) {
				price = lookupResult.price;
				//price = PriceGrab.getInstance().getPrice(tempName, 2);
				MethodProvider.log("Got price: " + price);
			}
		}
		else
			price = 0;
	}

	public void update(){
		int increase = 0;
		if(id==0)
			increase =  (int) (ctx.getInventory().count(name)- lastCount);
		else
			increase =  (int) (ctx.getInventory().count(id)- lastCount);
		if(increase < 0)
			increase = 0;
		amount+=increase;
		if(id==0)
			lastCount = (int) ctx.getInventory().count(name);
		else
			lastCount = (int) ctx.getInventory().count(id);
	}

	public void setName(String name){
		this.name = name;
	}
	public void setAmount(int amt){
		this.amount = amt;
	}
	public void setPrice(int price){
		this.price = price;
	}
	public String getName(){
		return name;
	}
	public int getAmount(){
		return amount;
	}

	public int getPrice(){
		return price;
	}
	public int getValue(){
		if(amount <= 0)
			return 0;
		return amount * price;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
