import java.util.Calendar;
import java.util.Scanner;

public class hardee {
	public static void main(String[] args) {
		String laptop = "";
		Double price = 0.0;
		Double tax = 0.0;
		Double subtotal = 0.0;
		Double total = 0.0;
		Double lineItem = 0.0;
		int trigger = 1;
		char cont = 'y';
		int qty = 0;
		int choice = 0;
		Scanner input = new Scanner( System.in );
		Calendar dateTime = Calendar.getInstance();
		String orderSummary = String.format( "ORDER SUMMARY:\n\n" + "Date:%tD" + "\n\nTime:%tr", dateTime, dateTime );

		//orderSummary = orderSummary +


		while( Character.toUpperCase( cont ) == 'Y' ) {
			System.out.printf( "%s%49s\n%s%61s\n%s%66s\n%s%72s\n%s%80S",
					"1. Apple MacBook Pro 13-inch (Retina Display)",
					"$1,999.00",
					"2. Acer C7 Chromebook (C710-2055)",
					"279.99",
					"3. Asus VivoBook S400CA-UH51",
					"639.99",
					"4. Dell Latitude 643OU",
					"1,349.00",
					"5. Dell XPS 10",
					"679.00" );

			System.out.printf( "\n\nEnter your choice:" );
			choice = input.nextInt();

			if( choice <= 0 || choice >= 6 ) {
				System.out.printf( "Invalid choice! Would you like to continue? Enter \"Y\" or \"N\"" );
				input.nextLine();
				cont = input.nextLine().charAt( 0 );
			}
			if( choice > 0 && choice < 6 ) {
				if( choice == 5 ) {
					laptop = "Dell XPS 10";
					price = 679.00;
				}
				if( choice == 4 ) {
					laptop = "Dell Latitude 643OU";
					price = 1349.00;
				}
				if( choice == 3 ) {
					laptop = "Asus VivoBook S400CA-UH51";
					price = 639.99;
				}
				if( choice == 2 ) {
					laptop = "Acer C7 Chromebook (C710-2055)";
					price = 279.99;
				}
				if( choice == 1 ) {
					laptop = "Apple MacBook Pro 13-inch (Retina Display)";
					price = 1999.00;
				}
				System.out.printf( "Enter the quanity for %s:", laptop );
				qty = input.nextInt();
				input.nextLine();

				lineItem = qty * price;
				subtotal = subtotal + lineItem;

				if( trigger == 1 ) {
					orderSummary += "\n\n" + qty + "  " + laptop + "   $" + + lineItem;
					trigger = 0;
				}

				if( trigger == 0 ) {
					orderSummary += "\n\n" + qty + "\n\n" + laptop + "\n\n" + lineItem;

				}
				System.out.printf( "Would you like to continue? Enter \"Y\" or \"N\"" );
				cont = input.nextLine().charAt( 0 );

				if( Character.toUpperCase( cont ) == 'N' )
					tax = subtotal * .0825;
				total = subtotal + tax;

				orderSummary = orderSummary + subtotal + tax + total;

				System.out.printf( "%s", orderSummary );

			}
		}


	}
}