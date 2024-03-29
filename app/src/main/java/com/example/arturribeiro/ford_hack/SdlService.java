package com.example.arturribeiro.ford_hack;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.smartdevicelink.exception.SdlException;
import com.smartdevicelink.exception.SdlExceptionCause;
import com.smartdevicelink.proxy.RPCRequest;
import com.smartdevicelink.proxy.SdlProxyALM;
import com.smartdevicelink.proxy.callbacks.OnServiceEnded;
import com.smartdevicelink.proxy.callbacks.OnServiceNACKed;
import com.smartdevicelink.proxy.interfaces.IProxyListenerALM;
import com.smartdevicelink.proxy.rpc.AddCommand;
import com.smartdevicelink.proxy.rpc.AddCommandResponse;
import com.smartdevicelink.proxy.rpc.AddSubMenuResponse;
import com.smartdevicelink.proxy.rpc.AlertManeuverResponse;
import com.smartdevicelink.proxy.rpc.AlertResponse;
import com.smartdevicelink.proxy.rpc.ChangeRegistrationResponse;
import com.smartdevicelink.proxy.rpc.CreateInteractionChoiceSetResponse;
import com.smartdevicelink.proxy.rpc.DeleteCommandResponse;
import com.smartdevicelink.proxy.rpc.DeleteFileResponse;
import com.smartdevicelink.proxy.rpc.DeleteInteractionChoiceSetResponse;
import com.smartdevicelink.proxy.rpc.DeleteSubMenuResponse;
import com.smartdevicelink.proxy.rpc.DiagnosticMessageResponse;
import com.smartdevicelink.proxy.rpc.DialNumberResponse;
import com.smartdevicelink.proxy.rpc.EndAudioPassThruResponse;
import com.smartdevicelink.proxy.rpc.GenericResponse;
import com.smartdevicelink.proxy.rpc.GetDTCsResponse;
import com.smartdevicelink.proxy.rpc.GetVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.GetWayPointsResponse;
import com.smartdevicelink.proxy.rpc.ListFiles;
import com.smartdevicelink.proxy.rpc.ListFilesResponse;
import com.smartdevicelink.proxy.rpc.MenuParams;
import com.smartdevicelink.proxy.rpc.OnAudioPassThru;
import com.smartdevicelink.proxy.rpc.OnButtonEvent;
import com.smartdevicelink.proxy.rpc.OnButtonPress;
import com.smartdevicelink.proxy.rpc.OnCommand;
import com.smartdevicelink.proxy.rpc.OnDriverDistraction;
import com.smartdevicelink.proxy.rpc.OnHMIStatus;
import com.smartdevicelink.proxy.rpc.OnHashChange;
import com.smartdevicelink.proxy.rpc.OnKeyboardInput;
import com.smartdevicelink.proxy.rpc.OnLanguageChange;
import com.smartdevicelink.proxy.rpc.OnLockScreenStatus;
import com.smartdevicelink.proxy.rpc.OnPermissionsChange;
import com.smartdevicelink.proxy.rpc.OnStreamRPC;
import com.smartdevicelink.proxy.rpc.OnSystemRequest;
import com.smartdevicelink.proxy.rpc.OnTBTClientState;
import com.smartdevicelink.proxy.rpc.OnTouchEvent;
import com.smartdevicelink.proxy.rpc.OnVehicleData;
import com.smartdevicelink.proxy.rpc.OnWayPointChange;
import com.smartdevicelink.proxy.rpc.PerformAudioPassThruResponse;
import com.smartdevicelink.proxy.rpc.PerformInteractionResponse;
import com.smartdevicelink.proxy.rpc.PutFile;
import com.smartdevicelink.proxy.rpc.PutFileResponse;
import com.smartdevicelink.proxy.rpc.ReadDIDResponse;
import com.smartdevicelink.proxy.rpc.ResetGlobalPropertiesResponse;
import com.smartdevicelink.proxy.rpc.ScrollableMessageResponse;
import com.smartdevicelink.proxy.rpc.SendLocationResponse;
import com.smartdevicelink.proxy.rpc.SetAppIconResponse;
import com.smartdevicelink.proxy.rpc.SetDisplayLayoutResponse;
import com.smartdevicelink.proxy.rpc.SetGlobalPropertiesResponse;
import com.smartdevicelink.proxy.rpc.SetMediaClockTimerResponse;
import com.smartdevicelink.proxy.rpc.ShowConstantTbtResponse;
import com.smartdevicelink.proxy.rpc.ShowResponse;
import com.smartdevicelink.proxy.rpc.SliderResponse;
import com.smartdevicelink.proxy.rpc.SpeakResponse;
import com.smartdevicelink.proxy.rpc.StreamRPCResponse;
import com.smartdevicelink.proxy.rpc.SubscribeButtonResponse;
import com.smartdevicelink.proxy.rpc.SubscribeVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.SubscribeWayPointsResponse;
import com.smartdevicelink.proxy.rpc.SystemRequestResponse;
import com.smartdevicelink.proxy.rpc.UnsubscribeButtonResponse;
import com.smartdevicelink.proxy.rpc.UnsubscribeVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.UnsubscribeWayPointsResponse;
import com.smartdevicelink.proxy.rpc.UpdateTurnListResponse;
import com.smartdevicelink.proxy.rpc.enums.FileType;
import com.smartdevicelink.proxy.rpc.enums.HMILevel;
import com.smartdevicelink.proxy.rpc.enums.LockScreenStatus;
import com.smartdevicelink.proxy.rpc.enums.SdlDisconnectedReason;
import com.smartdevicelink.proxy.rpc.enums.TextAlignment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

	public class SdlService extends Service implements IProxyListenerALM{

		FirebaseDatabase database = FirebaseDatabase.getInstance();
		DatabaseReference myRef = database.getReference("message");

		private Car car = new Car();

		private static final String TAG 					= "SDL Service";

		private static final String APP_NAME 				= "Ford Moto";
		private static final String APP_ID 					= "260537589";

		private static final String ICON_FILENAME 			= "hello_sdl_icon.png";
		private int iconCorrelationId;

		List<String> remoteFiles;

		private static final String WELCOME_SHOW 			= "Welcome to HelloSDL";
		private static final String WELCOME_SPEAK 			= "Welcome to Hello S D L";

		private static final String TEST_COMMAND_NAME 		= "Test Command";

		private static final String DATA_COMMAND_NAME 		= "Velocidade";

		private static final String CHECKIN_COMAND_NAME     = "Check In";
		private static final String CHECKOU_COMAND_NAME     = "Check Out";



		private static final int TEST_COMMAND_ID 			= 1;
		private static final int CHECKIN_COMAND_ID 			= 2;
		private static final int CHECKOUT_COMAND_ID 		= 3;


		// variable used to increment correlation ID for every request sent to SYNC
		public int autoIncCorrId = 0;
		// variable to contain the current state of the service
		private static SdlService instance = null;

		// variable to create and call functions of the SyncProxy
		private SdlProxyALM proxy = null;

		private boolean lockscreenDisplayed = false;

		private boolean firstNonHmiNone = true;
		private boolean isVehicleDataSubscribed = false;



		@Override
		public IBinder onBind(Intent intent) {
			return null;
		}

		@Override
		public void onCreate() {
			super.onCreate();
			instance = this;
			remoteFiles = new ArrayList<String>();
		}

		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			if (intent != null) {
				startProxy();
			}

			return START_STICKY;
		}

		@Override
		public void onDestroy() {
			disposeSyncProxy();
			//LockScreenManager.clearLockScreen();
			instance = null;
			super.onDestroy();
		}

		public static SdlService getInstance() {
			return instance;
		}

		public SdlProxyALM getProxy() {
			return proxy;
		}

		public void startProxy() {
			if (proxy == null) {
				try {
					proxy = new SdlProxyALM(this, APP_NAME, false, APP_ID);
				} catch (SdlException e) {
					e.printStackTrace();
					// error creating proxy, returned proxy = null
					if (proxy == null) {
						stopSelf();
					}
				}
			}
		}

		public void disposeSyncProxy() {
			if (proxy != null) {
				try {
					proxy.dispose();
				} catch (SdlException e) {
					e.printStackTrace();
				}
				proxy = null;
				//LockScreenManager.clearLockScreen();
			}
			this.firstNonHmiNone = true;
			this.isVehicleDataSubscribed = false;

		}

		public void reset() {
			if (proxy != null) {
				try {
					proxy.resetProxy();
					this.firstNonHmiNone = true;
					this.isVehicleDataSubscribed = false;
				} catch (SdlException e1) {
					e1.printStackTrace();
					//something goes wrong, & the proxy returns as null, stop the service.
					// do not want a running service with a null proxy
					if (proxy == null) {
						stopSelf();
					}
				}
			} else {
				startProxy();
			}
		}

		/**
		 * Will show a sample test message on screen as well as speak a sample test message
		 */
		public void showTest(){
			try {
				proxy.show(TEST_COMMAND_NAME, "Command has been selected", TextAlignment.CENTERED, autoIncCorrId++);
				proxy.speak(TEST_COMMAND_NAME, autoIncCorrId++);
			} catch (SdlException e) {
				e.printStackTrace();
			}
		}

		/**
		 *  Add commands for the app on SDL.
		 */
		public void sendCommands(){
			AddCommand command = new AddCommand();

			MenuParams params = new MenuParams();
			params.setMenuName(TEST_COMMAND_NAME);

			command = new AddCommand();
			command.setCmdID(TEST_COMMAND_ID);
			command.setMenuParams(params);
			command.setVrCommands(Arrays.asList(new String[]{TEST_COMMAND_NAME}));
			sendRpcRequest(command);


			params = new MenuParams();
			params.setMenuName(CHECKIN_COMAND_NAME);

			command = new AddCommand();
			command.setCmdID(CHECKIN_COMAND_ID);
			command.setMenuParams(params);
			command.setVrCommands(Arrays.asList(new String[]{CHECKIN_COMAND_NAME}));
			sendRpcRequest(command);

			params = new MenuParams();
			params.setMenuName(CHECKOU_COMAND_NAME);

			command = new AddCommand();
			command.setCmdID(CHECKOUT_COMAND_ID);
			command.setMenuParams(params);
			command.setVrCommands(Arrays.asList(new String[]{CHECKOU_COMAND_NAME}));
			sendRpcRequest(command);

			try {
				car.userConected = false;
				proxy.subscribevehicledata(true, true, true, true, true, true, true, true, true, true, true, true, true, true, autoIncCorrId++);
				proxy.getvehicledata(true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, autoIncCorrId++);
			} catch (SdlException e) {
				e.printStackTrace();
			}
		}

		public void getData(){
			try {
				proxy.getvehicledata(true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, autoIncCorrId++);
			} catch (SdlException e) {
				e.printStackTrace();
			}
	//		GetVehicleData command = new GetVehicleData();
	//
	//		// MenuParams params = new MenuParams();
	//		// params.setMenuName(DATA_COMMAND_NAME);
	//		command.setSpeed(true);
	//		sendRpcRequest(command);
		}

		public void checkIn() {
			try {
				proxy.show("Check in Ok", "" , TextAlignment.CENTERED, autoIncCorrId++);
				proxy.speak("Check in Ok", autoIncCorrId++);
				car.userConected = true;
				car.airbagStatus = true;
				car.ignition = true;
				setMyRef(myRef, car);
			} catch (SdlException e) {
				e.printStackTrace();
			}

		}

		public void checkOut() {
			try {
				proxy.show("Check Out Now", "", TextAlignment.CENTERED, autoIncCorrId++);
				proxy.speak("Check Out Now", autoIncCorrId++);
				car.userConected = false;
				car.velocity = 0.0;
				car.airbagStatus = true;
				car.ignition = false;
				this.setMyRef(myRef, car);
			} catch (SdlException e) {
				e.printStackTrace();
			}
		}

		public void setMyRef(DatabaseReference myRef, Car car) {

			this.myRef = myRef;
			this.myRef.child("car").child("1").setValue(car);
		}

		/**
		 * Sends an RPC Request to the connected head unit. Automatically adds a correlation id.
		 * @param request
		 */
		private void sendRpcRequest(RPCRequest request){
			request.setCorrelationID(autoIncCorrId++);
			try {
				proxy.sendRPCRequest(request);
			} catch (SdlException e) {
				e.printStackTrace();
			}
		}
		/**
		 * Sends the app icon through the uploadImage method with correct params
		 * @throws SdlException
		 */
		private void sendIcon() throws SdlException {
			iconCorrelationId = autoIncCorrId++;
			uploadImage(R.drawable.ic_launcherold, ICON_FILENAME, iconCorrelationId, true);
		}

		/**
		 * This method will help upload an image to the head unit
		 * @param resource the R.drawable.__ value of the image you wish to send
		 * @param imageName the filename that will be used to reference this image
		 * @param correlationId the correlation id to be used with this request. Helpful for monitoring putfileresponses
		 * @param isPersistent tell the system if the file should stay or be cleared out after connection.
		 */
		private void uploadImage(int resource, String imageName,int correlationId, boolean isPersistent){
			PutFile putFile = new PutFile();
			putFile.setFileType(FileType.GRAPHIC_PNG);
			putFile.setSdlFileName(imageName);
			putFile.setCorrelationID(correlationId);
			putFile.setPersistentFile(isPersistent);
			putFile.setSystemFile(false);
			putFile.setBulkData(contentsOfResource(resource));

			try {
				proxy.sendRPCRequest(putFile);
			} catch (SdlException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Helper method to take resource files and turn them into byte arrays
		 * @param resource
		 * @return
		 */
		private byte[] contentsOfResource(int resource) {
			InputStream is = null;
			try {
				is = getResources().openRawResource(resource);
				ByteArrayOutputStream os = new ByteArrayOutputStream(is.available());
				final int buffersize = 4096;
				final byte[] buffer = new byte[buffersize];
				int available = 0;
				while ((available = is.read(buffer)) >= 0) {
					os.write(buffer, 0, available);
				}
				return os.toByteArray();
			} catch (IOException e) {
				Log.w("SDL Service", "Can't read icon file", e);
				return null;
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		@Override
		public void onProxyClosed(String info, Exception e, SdlDisconnectedReason reason) {

			if(!(e instanceof SdlException)){
				Log.v(TAG, "reset proxy in onproxy closed");
				reset();
			}
			else if ((((SdlException) e).getSdlExceptionCause() != SdlExceptionCause.SDL_PROXY_CYCLED))
			{
				if (((SdlException) e).getSdlExceptionCause() != SdlExceptionCause.BLUETOOTH_DISABLED)
				{
					Log.v(TAG, "reset proxy in onproxy closed");
					reset();
				}
			}

			clearLockScreen();

			stopSelf();
		}

		@Override
		public void onOnHMIStatus(OnHMIStatus notification) {
			if(notification.getHmiLevel().equals(HMILevel.HMI_FULL)){
				if (notification.getFirstRun()) {
					// send welcome message if applicable
					performWelcomeMessage();
					sendCommands();
					// getData();
				}
				// Other HMI (Show, PerformInteraction, etc.) would go here
			}


			if(!notification.getHmiLevel().equals(HMILevel.HMI_NONE)
					&& firstNonHmiNone){
				sendCommands();
				//uploadImages();
				firstNonHmiNone = false;

				// Other app setup (SubMenu, CreateChoiceSet, etc.) would go here
			}else{
				//We have HMI_NONE
				if(notification.getFirstRun()){
					uploadImages();
				}
			}



		}

		/**
		 * Will show a sample welcome message on screen as well as speak a sample welcome message
		 */
		private void performWelcomeMessage(){
			try {
				//Set the welcome message on screen
				proxy.show(APP_NAME, WELCOME_SHOW, TextAlignment.CENTERED, autoIncCorrId++);

				//Say the welcome message
				proxy.speak(WELCOME_SPEAK, autoIncCorrId++);

			} catch (SdlException e) {
				e.printStackTrace();
			}

		}

		/**
		 *  Requests list of images to SDL, and uploads images that are missing.
		 */
		private void uploadImages(){
			ListFiles listFiles = new ListFiles();
			this.sendRpcRequest(listFiles);

		}

		@Override
		public void onListFilesResponse(ListFilesResponse response) {
			Log.i(TAG, "onListFilesResponse from SDL ");
			if(response.getSuccess()){
				remoteFiles = response.getFilenames();
			}

			// Check the mutable set for the AppIcon
			// If not present, upload the image
			if(remoteFiles== null || !remoteFiles.contains(SdlService.ICON_FILENAME)){
				try {
					sendIcon();
				} catch (SdlException e) {
					e.printStackTrace();
				}
			}else{
				// If the file is already present, send the SetAppIcon request
				try {
					proxy.setappicon(ICON_FILENAME, autoIncCorrId++);
				} catch (SdlException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onPutFileResponse(PutFileResponse response) {
			Log.i(TAG, "onPutFileResponse from SDL");
			if(response.getCorrelationID().intValue() == iconCorrelationId){ //If we have successfully uploaded our icon, we want to set it
				try {
					proxy.setappicon(ICON_FILENAME, autoIncCorrId++);
				} catch (SdlException e) {
					e.printStackTrace();
				}
			}

		}

		@Override
		public void onOnLockScreenNotification(OnLockScreenStatus notification) {
			if(!lockscreenDisplayed && notification.getShowLockScreen() == LockScreenStatus.REQUIRED){
				// Show lock screen
				Intent intent = new Intent(getApplicationContext(), LockScreenActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_ACTIVITY_NEW_TASK);
				lockscreenDisplayed = true;
				startActivity(intent);
			} else if(lockscreenDisplayed && notification.getShowLockScreen() != LockScreenStatus.REQUIRED){
				// Clear lock screen
				clearLockScreen();
			}
		}

		private void clearLockScreen() {
			Intent intent = new Intent(getApplicationContext(), MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			lockscreenDisplayed = false;
		}

		@Override
		public void onOnCommand(OnCommand notification){
			Integer id = notification.getCmdID();
			if(id != null){
				switch(id) {
				case CHECKIN_COMAND_ID:
					checkIn();
					break;
				case CHECKOUT_COMAND_ID:
					checkOut();
					break;
				}
				//onAddCommandClicked(id);
			}
		}

		/**
		 *  Callback method that runs when the add command response is received from SDL.
		 */
		@Override
		public void onAddCommandResponse(AddCommandResponse response) {
			Log.i(TAG, "AddCommand response from SDL: " + response);

		}


		/*  Vehicle Data   */


		@Override
		public void onOnPermissionsChange(OnPermissionsChange notification) {
			Log.i(TAG, "Permision changed: " + notification);
			/* Uncomment to subscribe to vehicle data
			List<PermissionItem> permissions = notification.getPermissionItem();
			for(PermissionItem permission:permissions){
				if(permission.getRpcName().equalsIgnoreCase(FunctionID.SUBSCRIBE_VEHICLE_DATA.name())){
					if(permission.getHMIPermissions().getAllowed()!=null && permission.getHMIPermissions().getAllowed().size()>0){
						if(!isVehicleDataSubscribed){ //If we haven't already subscribed we will subscribe now
							//TODO: Add the vehicle data items you want to subscribe to
							//proxy.subscribevehicledata(gps, speed, rpm, fuelLevel, fuelLevel_State, instantFuelConsumption, externalTemperature, prndl, tirePressure, odometer, beltStatus, bodyInformation, deviceStatus, driverBraking, correlationID);
							proxy.subscribevehicledata(false, true, rpm, false, false, false, false, false, false, false, false, false, false, false, autoIncCorrId++);
						}
					}
				}
			}
			*/
		}

		@Override
		public void onSubscribeVehicleDataResponse(SubscribeVehicleDataResponse response) {
			if(response.getSuccess()){

				//car.velocity = response.getSpeed();
				//car.ignition = response.getBodyInformation().getIgnitionStatus();
				//car.doors = response.getBodyInformation().getPassengerDoorAjar();
				//car.airbagStatus = response.getAirbagStatus();////car.fuelLevel = response.getFuelLevel();
				//car.odometer = response.getOdometer();

				setMyRef(myRef, car);
			}
		}

		@Override
		public void onOnVehicleData(OnVehicleData notification) {

			car.velocity = notification.getSpeed();

			setMyRef(myRef, car);
		}

		/**
		 * Rest of the SDL callbacks from the head unit
		 */

		@Override
		public void onAddSubMenuResponse(AddSubMenuResponse response) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onCreateInteractionChoiceSetResponse(CreateInteractionChoiceSetResponse response) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onAlertResponse(AlertResponse response) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onDeleteCommandResponse(DeleteCommandResponse response) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onDeleteInteractionChoiceSetResponse(DeleteInteractionChoiceSetResponse response) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onDeleteSubMenuResponse(DeleteSubMenuResponse response) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onPerformInteractionResponse(PerformInteractionResponse response) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onResetGlobalPropertiesResponse(
				ResetGlobalPropertiesResponse response) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSetGlobalPropertiesResponse(SetGlobalPropertiesResponse response) {
		}

		@Override
		public void onSetMediaClockTimerResponse(SetMediaClockTimerResponse response) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onShowResponse(ShowResponse response) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSpeakResponse(SpeakResponse response) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onOnButtonEvent(OnButtonEvent notification) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onOnButtonPress(OnButtonPress notification) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSubscribeButtonResponse(SubscribeButtonResponse response) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onUnsubscribeButtonResponse(UnsubscribeButtonResponse response) {
			// TODO Auto-generated method stub
		}


		@Override
		public void onOnTBTClientState(OnTBTClientState notification) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onUnsubscribeVehicleDataResponse(
				UnsubscribeVehicleDataResponse response) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetVehicleDataResponse(GetVehicleDataResponse response) {
			try {
				String teste = response.getSpeed().toString();
				proxy.show("teste", teste, TextAlignment.CENTERED, autoIncCorrId++);
			} catch (SdlException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onReadDIDResponse(ReadDIDResponse response) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetDTCsResponse(GetDTCsResponse response) {
			// TODO Auto-generated method stub

		}


		@Override
		public void onPerformAudioPassThruResponse(PerformAudioPassThruResponse response) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onEndAudioPassThruResponse(EndAudioPassThruResponse response) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onOnAudioPassThru(OnAudioPassThru notification) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onDeleteFileResponse(DeleteFileResponse response) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSetAppIconResponse(SetAppIconResponse response) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onScrollableMessageResponse(ScrollableMessageResponse response) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onChangeRegistrationResponse(ChangeRegistrationResponse response) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSetDisplayLayoutResponse(SetDisplayLayoutResponse response) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onOnLanguageChange(OnLanguageChange notification) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSliderResponse(SliderResponse response) {
			// TODO Auto-generated method stub

		}


		@Override
		public void onOnHashChange(OnHashChange notification) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onOnSystemRequest(OnSystemRequest notification) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSystemRequestResponse(SystemRequestResponse response) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onOnKeyboardInput(OnKeyboardInput notification) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onOnTouchEvent(OnTouchEvent notification) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onDiagnosticMessageResponse(DiagnosticMessageResponse response) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onOnStreamRPC(OnStreamRPC notification) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStreamRPCResponse(StreamRPCResponse response) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onDialNumberResponse(DialNumberResponse response) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSendLocationResponse(SendLocationResponse response) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onServiceEnded(OnServiceEnded serviceEnded) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onServiceNACKed(OnServiceNACKed serviceNACKed) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onShowConstantTbtResponse(ShowConstantTbtResponse response) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAlertManeuverResponse(AlertManeuverResponse response) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onUpdateTurnListResponse(UpdateTurnListResponse response) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onServiceDataACK(int i) {

		}

		@Override
		public void onGetWayPointsResponse(GetWayPointsResponse getWayPointsResponse) {

		}

		@Override
		public void onSubscribeWayPointsResponse(SubscribeWayPointsResponse subscribeWayPointsResponse) {

		}

		@Override
		public void onUnsubscribeWayPointsResponse(UnsubscribeWayPointsResponse unsubscribeWayPointsResponse) {

		}

		@Override
		public void onOnWayPointChange(OnWayPointChange onWayPointChange) {

		}

		// @Override
		public void onServiceDataACK() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onOnDriverDistraction(OnDriverDistraction notification) {
			// Some RPCs (depending on region) cannot be sent when driver distraction is active.
		}

		@Override
		public void onError(String info, Exception e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onGenericResponse(GenericResponse response) {
			// TODO Auto-generated method stub
		}

	}
