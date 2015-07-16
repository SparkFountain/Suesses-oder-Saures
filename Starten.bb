AppTitle("Süßes oder Saures!")
SeedRnd(MilliSecs())

Global screenWidth = 580
Global screenHeight = 620
Global screenDepth = 32
Global screenMode = 2
Global fps = 60

Graphics(screenWidth,screenHeight,screenDepth,screenMode)
SetBuffer(BackBuffer())
Global frameTimer = CreateTimer(fps)


;variables
Global doInit = 1
Global level = 1

Const numOfLevels = 20

Dim gameField(14,14)
Const numOfHouses = 4
Dim houses(numOfHouses-1)
For i=1 To numOfHouses
	houses(i-1) = LoadAnimImage("gfx/house"+(i-1)+".png",32,32,0,4)
	MaskImage(houses(i-1),255,0,255)
Next

Global hulk = LoadAnimImage("gfx/hulk.png",32,32,0,4)
MaskImage(hulk,255,0,255)

Global lastLevel = LoadImage("gfx/lastlevel.png")
MaskImage(lastLevel,255,0,255)
Global nextLevel = LoadImage("gfx/nextlevel.png")
MaskImage(nextLevel,255,0,255)


Global editorField = 0	; 0=nothing, 1=house, 2=way, 3=player
Global fieldsLeft
Global playerX, playerY, lastMove$ = ""

Global gfxPlayer = LoadAnimImage("gfx/player.png",32,32,0,4)
MaskImage(gfxPlayer,255,0,255)

Global haha = LoadSound("sfx/haha.mp3")
Global ouch = LoadSound("sfx/ouch.mp3")

Dim bell(3)
For i=0 To 3
	bell(i) = LoadSound("sfx/bell"+(i+1)+".mp3")
Next

Dim walk(2)
For i=0 To 2
	walk(i) = LoadSound("sfx/walk"+(i+1)+".mp3")
Next


Global bigFont = LoadFont("Comic Sans MS", 40, 1)
Global smallFont = LoadFont("Comic Sans MS", 30)

ClsColor(0,0,128)


;MAIN LOOP
Repeat
	
	Cls()
	WaitTimer(frameTimer)
	
	If(KeyHit(1)) Then End()
	
	;collect all sweets, but do not pass one house twice!
	If(doInit) Then
		Init(level)
		doInit=0
	EndIf
	
	DrawGameField(False)
	Move()
	ChangeLevel()
	;Editor()
	
	Color(243,102,0) : SetFont(bigFont)
	Text(screenWidth/2,0,"Level: "+level,screenWidth/2)
	
	SetFont(smallFont)
	Text(screenWidth/2,50,"noch "+fieldsLeft+" Häuser",screenWidth/2)
	
	Text(screenWidth/2,screenHeight-35,"Drücke R zum Neustarten",screenWidth/2)
	
	Flip(0)
	
Forever

Function Init(lvl)
	;reset variables
	lastMove = ""
	fieldsLeft = 0
	
	LoadLevel(lvl)
End Function

Function LoadLevel(lvl, ingame=True)
	If(FileType("sav/"+lvl) <> 1) Then
		DebugLog("level doesn't exist. try again.")
	Else
		stream = ReadFile("sav/"+lvl)
		For x=0 To 14
			For y=0 To 14
				gameField(x,y) = ReadByte(stream)
				If(ingame And (gameField(x,y) = 3)) Then
					playerX = x
					playerY = y
				ElseIf(gameField(x,y)=1) Then
					gameField(x,y) = Rnd(0,numOfHouses-1)+5
					DebugLog(gameField(x,y))
					fieldsLeft=fieldsLeft+1
				EndIf
			Next
		Next
		CloseFile(stream)
	EndIf
End Function

Function DrawGameField(edit=False)
	
	For x=0 To 14
		For y=0 To 14
			Select gameField(x,y)
				Case 0 : Color(0,0,0)
				Case 1 : Color(255,0,0)
				Case 2 : Color(128,128,128)
				Case 3
					If(edit) Then 
						Color(0,255,0) 
					Else 
						Color(128,128,128)
					EndIf
				Case 4
					Color(128,128,128)
					Rect(x*32+50,y*32+100,32,32)
					If(Not(edit)) Then
						If(MilliSecs() Mod 800 < 200) Then
							DrawImage(hulk,x*32+50,y*32+100,0)
						ElseIf(MilliSecs() Mod 800 < 400) Then
							DrawImage(hulk,x*32+50,y*32+100,1)
						ElseIf(MilliSecs() Mod 800 < 600) Then
							DrawImage(hulk,x*32+50,y*32+100,2)
						Else
							DrawImage(hulk,x*32+50,y*32+100,3)
						EndIf
					EndIf
				Case 5,6,7,8
					Color(128,128,128)
					Rect(x*32+50,y*32+100,32,32)
					If(Not(edit)) Then
						If(MilliSecs() Mod 800 < 200) Then
							DrawImage(houses(gameField(x,y)-5),x*32+50,y*32+100,0)
						ElseIf(MilliSecs() Mod 800 < 400) Then
							DrawImage(houses(gameField(x,y)-5),x*32+50,y*32+100,1)
						ElseIf(MilliSecs() Mod 800 < 600) Then
							DrawImage(houses(gameField(x,y)-5),x*32+50,y*32+100,2)
						Else
							DrawImage(houses(gameField(x,y)-5),x*32+50,y*32+100,3)
						EndIf
					EndIf
			End Select
			If(gameField(x,y)<>0 And gameField(x,y)<=3) Then Rect(x*32+50,y*32+100,32,32)
		Next
	Next
	
	If(Not(edit)) Then DrawImage(gfxPlayer,playerX*32+50,playerY*32+100,((MilliSecs() Mod 800)/200))
End Function

Function Move()
	
	If((KeyHit(17) Or KeyHit(200)) And lastMove<>"down" And gameField(playerX,playerY-1)>0 And playerY>0) Then
		If(gameField(playerX,playerY-1)=4) Then
			PlaySound(ouch)
			Delay(900)
			doInit=True
		EndIf
		lastMove = "up"
		playerY=playerY-1
		UpdateGameField(playerX,playerY)
	ElseIf((KeyHit(31) Or KeyHit(208)) And lastMove<>"up" And gameField(playerX,playerY+1)>0 And playerY<14) Then
		If(gameField(playerX,playerY+1)=4) Then
			PlaySound(ouch)
			Delay(900)
			doInit=True
		EndIf
		lastMove = "down"
		playerY=playerY+1
		UpdateGameField(playerX,playerY)
	ElseIf((KeyHit(30) Or KeyHit(203)) And lastMove<>"right" And gameField(playerX-1,playerY)>0 And playerX>0) Then
		If(gameField(playerX-1,playerY)=4) Then
			PlaySound(ouch)
			Delay(900)
			doInit=True
		EndIf
		lastMove = "left"
		playerX=playerX-1
		UpdateGameField(playerX,playerY)
	ElseIf((KeyHit(32) Or KeyHit(205)) And lastMove<>"left" And gameField(playerX+1,playerY)>0 And playerX<14) Then
		If(gameField(playerX+1,playerY)=4) Then
			PlaySound(ouch)
			Delay(900)
			doInit=True
		EndIf
		lastMove = "right"
		playerX=playerX+1
		UpdateGameField(playerX,playerY)
	EndIf
	
	If(KeyHit(19)) Then doInit=True
End Function

Function UpdateGameField(x,y)
	;walk sound
	PlaySound(walk(Rnd(0,2)))
	
	;if a house stands there, set it to "visited" (4)
	If(gameField(x,y) >= 5) Then
		gameField(x,y) = 4
		fieldsLeft=fieldsLeft-1
		
		If(fieldsLeft=0) Then
			If(level<numOfLevels) Then
				level=level+1
			Else
				level=1
			EndIf
			doInit=True
			PlaySound(haha)
			;Delay(1700)
		Else
			PlaySound(bell(Rnd(0,3)))
		EndIf
	EndIf
End Function

Function ChangeLevel()
	DrawImage(lastLevel,20,10)
	DrawImage(nextLevel,screenWidth-20-48,10)
	
	If(MouseHit(1)) Then
		Local mx = MouseX()
		Local my = MouseY()
		If(mx>=20 And mx<=20+48 And my>=10 And my<=10+48) Then
			fieldsLeft=0
			If(level>1) Then
				level=level-1
			Else
				level=numOfLevels
			EndIf
			doInit=True
		ElseIf(mx>=screenWidth-20-48 And mx<=screenWidth-20 And my>=10 And my<=10+48) Then
			fieldsLeft=0
			If(level<numOfLevels) Then
				level=level+1
			Else
				level=1
			EndIf
			doInit=True
		EndIf
	EndIf
End Function

Function Editor()
	If(MouseDown(1)) Then
		Local mx = (MouseX()-50)/32
		Local my = (MouseY()-100)/32
		
		If(mx>=0 And mx<15 And my>=0 And my<15) Then
			gameField(mx,my) = editorField
		EndIf
	EndIf
	
	If(KeyHit(2)) Then editorField = 0
	If(KeyHit(3)) Then editorField = 1
	If(KeyHit(4)) Then editorField = 2
	If(KeyHit(5)) Then editorField = 3
	
	Select editorField
		Case 0 : Color(0,0,0)
		Case 1 : Color(255,0,0)
		Case 2 : Color(128,128,128)
		Case 3 : Color(0,255,0)
	End Select
	Rect(16*32,0,32,32)
	
	Color(255,255,255)
	;save
	If(KeyHit(60)) Then
		FlushKeys() : FlushMouse() : Locate(0,0)
		Local name$ = Input("Levelname to save: ")
		If(name = "none") Then
			DebugLog("ok, don't save the level.")
		Else
			stream = WriteFile("sav/"+name)
			For x=0 To 14
				For y=0 To 14
					WriteByte(stream,gameField(x,y))
				Next
			Next
			CloseFile(stream)
		EndIf
	EndIf
	
	;load
	If(KeyHit(59)) Then
		FlushKeys() : FlushMouse() : Locate(0,0)
		name$ = Input("Levelname to load: ")
		LoadLevel(Int(name))
	EndIf
End Function
;~IDEal Editor Parameters:
;~F#5F#67
;~C#Blitz3D