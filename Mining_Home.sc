Program Mining_Home;
const
mineX = '1891,1919';    //'minX,maxX' мин и макс Х координаты шахты
mineY = '357,376';      //'minY,maxY' мин и макс Y координаты шахты
forgeXY = '1955,366';   //'X,Y' плавильни, где будем выплавлять инги из руды
IDTools = $40020C95;    //ID контейнера откуда будем брать инструмен и бинты
IDArms = $40020C8D;     //ID контейнера откуда будем брать армор, щит и оружие
IDBW = $40091DD8;       //ID кувшина с водой где будем мыть бинты
//Type армора, оружия и щита
TypeArms = '$1412,$1413,$1415,$1410,$1414,$1411,$0F5E,$1B74';
{1.шлем | 2.горгетка | 3.тело | 4.плечи | 5.перчатки | 6.ноги | 7.оружие  | 8.щит}
// !! НЕ МЕНЯЙТЕ ПОРЯДОК ВЕЩЕЙ В СКРИПТЕ !!
CountBandage = 50;       //Кол-во бандажек которые берём с собой



var
minX, maxX, minY, maxY, forgeX, forgeY : Word;
TA : array of Word;






function GetSettings : Boolean;
var
SL : TStringList;
txtConst : array of String;
nameConst : array of String;
i : Integer;
begin
    result := False;
    txtConst := [mineX, mineY, forgeXY]; 
    nameConst := ['mineX','mineY', 'forgeX', 'waterXY'];
    for i := 0 to High(txtConst) do
    begin
        SL := TStringList.Create;
        StrBreakApart(txtConst[i], ',', SL);
        if (SL.Count and 1) <> 0 then 
        begin
            AddToSystemJournal('Error: Проверьте настройки "'+nameConst[i]+'"!');
            exit;
        end;
        if i = 0 then             
        begin
            minX := StrToInt(SL.Strings[0]);
            maxX := StrToInt(SL.Strings[1]); 
        end;
        if i = 1 then
        begin
            minY := StrToInt(SL.Strings[0]);
            maxY := StrToInt(SL.Strings[1]);
        end;
        if i = 2 then
        begin
            forgeX := StrToInt(SL.Strings[0]);
            forgeY := StrToInt(SL.Strings[1]);
        end;     
        SL.Free;
    end; 
    SL := TStringList.Create;
    StrBreakApart(TypeArms, ',', SL);
    if SL.Count > 0 then
    begin
        SetLength(TA, SL.Count);
        for i := 0 to SL.Count - 1 do
        begin
           TA[i] := StrToInt(SL.Strings[i]);       
        end;
    end;
    SL.Free;
    result := True;
end;

function GoToTools : Boolean;
var
t : Integer;
begin
    result := True;
    t := 0;
    while (GetDistance(IDTools) > 1) and (t <= 15) do
    begin                                    
        CheckLag(60000);
        newMoveXY(GetX(IDTools), GetY(IDTools), True, 1, True);
        wait(1000);
        t := t + 1;
    end; 
    if t >= 15 then
    begin
        result := False;     
        AddToSystemJournal('ERROR: Не могу подойти к ящику с инструментами!');
    end;
end;

function CheckShovel : Boolean;
var
t : Integer;
begin
    result := False;
    FindTypeEx($0F39, $FFFF, Backpack, True);
    if FindCount <= 0 then
    begin
        if not GoToTools then exit;
        t := 0;
        while (LastContainer <> IDTools) and (t <= 15) do
        begin                 
            UseObject(IDTools);
            CheckLag(60000);
            wait(800);
            t := t + 1;        
        end;
        FindTypeEx($0F39, $FFFF, IDTools, True);
        if (FindCount <= 0) or (t >= 15) then 
        begin
            AddToSystemJournal('ERROR: У меня нет ЛОПАТ!');
            exit;
        end; 
        t := 0;  
        repeat
            FindTypeEx($0F39, $FFFF, IDTools, True);
            if FindCount > 0 then 
            begin
                CheckLag(60000);
                MoveItem(FindItem, 1, Backpack, 0,0,0); 
                t := t + 1;
                wait(800); 
            end;
        until (t >= 2) or (FindCount <= 0);            
    end;
    result := True;
end;

function CheckBloodBandage : Boolean;
var 
t : Integer;
ctime : TDateTime;
begin
    result := False;
    FindTypeEx($0E22, $FFFF, Backpack, False);
    while FindCount > 0 do
    begin
        if not newMoveXY(GetX(IDBW), GetY(IDBW), True, 2, True) then
        begin
            AddToSystemJournal('ERROR: Не могу добраться до "воды"!');  
            exit;
        end;  
        ctime := Now;
        if TargetPresent then CancelTarget;
        if Dead then exit;
        CheckLag(60000);
        UseObject(FindItem);
        t := 0; 
        repeat           
            t := t + 1;
            wait(100);
        until (InJournalBetweenTimes('Who will you use the bandages on?', ctime, Now) <> -1) or (t >= 50);
        WaitTargetObject(IDBW);
        wait(1000);
        FindTypeEx($0E22, $FFFF, Backpack, False);
    end;
    result := True;
end;

function CheckBandage : Boolean;
var
c, t : Integer;
begin
    result := False;
    FindTypeEx($0E21, $FFFF, Backpack, True);
    if FindFullQuantity < CountBandage then 
    begin  
        c := FindFullQuantity;
        if not GoToTools then exit;
        t := 0;
        while (LastContainer <> IDTools) and (t <= 15) do
        begin                 
            UseObject(IDTools);
            CheckLag(60000);
            wait(800);
            t := t + 1;        
        end;
        FindTypeEx($0E21, $FFFF, IDTools, True);
        if (FindCount <= 0) or (t >= 15) then 
        begin
            AddToSystemJournal('ERROR: У меня нет БИНТОВ!');
            exit;
        end;
        if c < CountBandage then
        begin
            FindTypeEx($0E21, $FFFF, IDTools, False);   
            if FindCount > 0 then
            begin  
                CheckLag(60000);
                MoveItem(FindItem, CountBandage-c, Backpack, 0,0,0);
                wait(800);
            end; 
        end;
    end; 
    result := True;
end;

function CheckTools : Boolean;
begin
    result := True; 
    if not CheckShovel then exit;
    result := False;
end;

function GoToArms : Boolean;
var
t : Integer;
begin
    result := True;
    t := 0;
    while (GetDistance(IDArms) > 1) and (t <= 15) do
    begin                                    
        CheckLag(60000);
        newMoveXY(GetX(IDArms), GetY(IDArms), True, 1, True);
        wait(1000);
        t := t + 1;
    end; 
    if t >= 15 then
    begin
        result := False;     
        AddToSystemJournal('ERROR: Не могу подойти к ящику с армором!');
    end;
end;

function CheckArmLayer : Boolean;
var
i, t : Integer;
itemLayer : array of Cardinal;
txt : array of String;
begin
    result := False;
    itemLayer := [HatLayer,NeckLayer,TorsoLayer,ArmsLayer,GlovesLayer,PantsLayer,RhandLayer,LhandLayer]; 
    txt := ['Шлема','Горгетки','Тела','Плеч','Перчаток','Ног','Оружия','Щита'];
    for i := 0 to High(itemLayer) do
    begin  
        if ObjAtLayer(itemLayer[i]) <= 0 then
        begin
            FindTypeEx(TA[i], $FFFF, Backpack, True);
            if FindCount <= 0 then
            begin
                if not GoToArms then exit;
                t := 0;
                while (LastContainer <> IDArms) and (t <= 15) do
                begin                 
                    UseObject(IDArms);
                    CheckLag(60000);
                    wait(800);
                    t := t + 1;        
                end;
                FindTypeEx(TA[i], $FFFF, IDArms, True);
                if FindCount <= 0 then 
                begin
                   AddToSystemJournal('ERROR: У меня нет - ['+txt[i]+']');
                   exit;
                end;
                CheckLag(60000);
                MoveItem(FindItem, 1, Backpack, 0,0,0);
                wait(800);
                FindTypeEx(TA[i], $FFFF, Backpack, True);
            end;
            if FindCount > 0 then 
            begin       
                CheckLag(60000);
                Equip(itemLayer[i], FindItem);
                wait(800);
            end;
        end;
    end;
    result := True;
end;


function GetResource : Boolean;
begin
    result := False;
    if not CheckArmLayer then exit;
    result := True;
end;







function GoToOutHome : Boolean;
begin
    result := False;
    if GetZ(Self) >= 7 then
    begin
        if not newMoveXY(1956, 369, True, 0, True) then
        begin
            AddToSystemJournal('ERROR: не могу выйти из дома!');
            exit;
        end;   
        repeat
            FindTypeEx($06A6, $FFFF, Ground, False);
            if FindCount > 0 then
            begin                   
                CheckLag(60000);
                UseObject(FindItem);
                wait(1000);
            end;
        until FindCount <= 0;
    end;
    result := True;
end;

function GoToInHome : Boolean;
begin
    result := False;
    if GetZ(Self) < 7 then
    begin
        if not newMoveXY(1956, 367, True, 0, True) then
        begin
            AddToSystemJournal('ERROR: не могу войти в дом!');
            exit;
        end;   
        repeat
            FindTypeEx($06A6, $FFFF, Ground, False);
            if FindCount > 0 then
            begin                   
                CheckLag(60000);
                UseObject(FindItem);
                wait(1000);
            end;
        until FindCount <= 0;
    end;
    result := True;
end;








Begin
    MoveOpenDoor := True;
    GoToOutHome; 
    GoToInHome;
    if not GetSettings then exit; 

    CheckBandage;
End.