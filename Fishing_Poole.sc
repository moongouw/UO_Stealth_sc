Program Fishing_Poole;

const
Pole = $0DBF;

type
    TileRecord = Record
    t,x,y,z : integer;
end;
var
Tile : array of TileRecord;
i : Integer;



function GetTiles : Boolean;
var
a : TStaticCell;
TileType : array [0..6] of Integer;
r, x, y, i, c, w : Integer;
begin
    result := false; 
    TileType[0] := 6038;
    TileType[1] := 6039;
    TileType[2] := 6040;
    TileType[3] := 6041;
    TileType[4] := 6042;
    TileType[5] := 6043;
    TileType[6] := 6044;
    r := 4;
    w := 0;
    for x := GetX(Self) - r to GetX(Self) + r do
    begin
        for y := GetY(Self) - r to GetY(Self) + r do
        begin
            a := ReadStaticsXY(x,y, WorldNum);
            if a.StaticCount > 0 then
            begin
                c := a.StaticCount - 1;
                for i := 0 to 6 do
                begin
                    if TileType[i] = a.Statics[c].Tile then
                    begin 
                        SetLength(Tile, w + 1);    
                        Tile[w].t := TileType[i];
                        Tile[w].x := x;
                        Tile[w].y := y;
                        Tile[w].z := a.Statics[c].z;
                        w := w + 1;
                        break;
                    end;
                end; 
            end;
        end; 
    end;
    if High(Tile) < 0 then
    begin
        AddToSystemJournal('ERROR: Не нашлось тайлов для ловли в радиусе '+IntToStr(r)+' клеток!');
        exit;
    end; 
    result := true;
    AddToSystemJournal('Радиус поиска: '+IntToStr(r));    
    AddToSystemJournal('Нашли тайлов для ловли: '+IntToStr(Length(Tile)));
end;

function CheckResour : Boolean;
var
c : Integer;
begin
    result := False;
    c := 0;
    if (GetType(ObjAtLayer(LhandLayer)) = Pole) then c := c + 1;
    FindTypeEx(Pole, $FFFF, Backpack, False);
    if FindCount > 0 then c := c + FindCount;
    if c > 0 then result := True;
    if c <= 0 then AddToSystemJournal('ERROR: Нет удочек!');
end;

function Fishing (tile, x, y, z : Integer) : Boolean;
var
ctime : TDateTime;
txtR, txtF : String;
t : Integer;
begin
    result := False;
    txtR := 'You stop fishing.|seem to get any fish here.'; 
    txtF := 'seem to get any fish here.';
    repeat
        if not CheckResour then exit;
        if (GetType(ObjAtLayer(LhandLayer)) <> Pole) then
        begin
            if (ObjAtLayer(RhandLayer) > 0) or (ObjAtLayer(LhandLayer) > 0) then
            begin      
                CheckLag(60000);
                Disarm;         
                Wait(500);
            end;
            FindTypeEx(Pole, $FFFF, Backpack, False);
            if FindCount >= 0 then
            begin           
                CheckLag(60000);
                Equip(LhandLayer,FindItem);
                wait(500);
            end;
        end;
        ctime := Now;
        CheckLag(60000);
        UseObject(ObjAtLayerEx(LhandLayer,self));
        WaitForTarget(5000);
        if TargetPresent then TargetToTile(tile, x, y, z);
        t := 0;
        repeat 
            wait(1000);
            t := t + 1;
        until (InJournalBetweenTimes(txtR, ctime, Now) <> -1) or (t >= 120);
    until (InJournalBetweenTimes(txtF, ctime, Now) <> -1);
    result := True;
end;

begin
    if not GetTiles then exit;
    repeat     
        for i := 0 to High(Tile) do
        begin
            if not Fishing(Tile[i].t, Tile[i].x, Tile[i].y, Tile[i].z) then exit;
        end;
    until dead;    
end.



{
type 
---
food fish
$09CD
$09CC
$4307
$4303
$09CE
$4306
$44C4
$44C6
---
magic fish
type $0DD6
color
$004C
$0056
$0042 
$0033 
---
shoes
$170D
$1711
$170B
$170F
}

