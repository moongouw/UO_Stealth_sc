Program Magery;
{$Include 'all.inc'}


function CheckSkill : String;
var
GCast : array [0..5] of String;
GMagery : array [0..5] of Integer;
//GRegs : array [0..5] of Integer;
i : Integer;
begin
    GCast[5] := 'Bless';
    GCast[4] := 'Recall';
    GCast[3] := 'Incognito';
    GCast[2] := 'Mark';
    GCast[1] := 'Flame Strike';
    GCast[0] := 'Resurrection';
    GMagery[5] := 30;
    GMagery[4] := 40;
    GMagery[3] := 50;
    GMagery[2] := 60;
    GMagery[1] := 70;
    GMagery[0] := 80;
    for i := 0 to 5 do
    begin
        if GetSkillValue('Magery') >= GMagery[i] then
        begin
            result := GCast[i];
            if GetSkillValue('Magery') >= 100 then result := 'Stop';
            exit;
        end;
    end;
end;

procedure UseMedit;
var
tm : TDateTime;
t : Integer;
begin
    repeat
        Hungry(0,backpack);
        tm := Now;
        t := 0;
        UseSkill('Meditation');
        wait(1000); 
        repeat
            wait(100);
            t := t + 1;
            if (InJournalBetweenTimes('You cannot focus your concentration', tm, Now) <> -1) then wait (11000);
        until (InJournalBetweenTimes('You are at peace|You cannot focus your concentration|You enter a meditative trance', tm, Now) <> -1) or (t >= 350)
        if (InJournalBetweenTimes('You enter a meditative trance', tm, Now) <> -1) then 
        repeat
            wait(1000);
        until GetMana(Self) >= GetMaxMana(Self);              
    until GetMana(Self) >= GetMaxMana(Self);
end;

procedure Proces;
var
t : Integer;
tm : TDateTime;
begin
    while CheckSkill <> 'Stop' do
    begin
        tm := Now;
        Cast(CheckSkill);
        CheckLag(60000);
        t := 0;
        repeat
            wait(100);
            t := t + 1;
            CheckSave;
        until TargetPresent or (InJournalBetweenTimes('Недостаточно Маны.|Заклятие срывается.|Your new name is|Your name is a sublight, again.', tm, Now) <> -1) or (t >= 150);
        t := 0;
        CheckLag(60000);
        repeat
            wait(100);
            t := t + 1;
            CheckSave;
            if TargetPresent then CancelTarget;
        until (TargetPresent = False) or (InJournalBetweenTimes('Недостаточно Маны.|Заклятие срывается.|Your new name|Your name is a sublight, again.', tm, Now) <> -1) or (t >= 150);
         if (InJournalBetweenTimes('Недостаточно Маны.', tm, Now) <> -1) then UseMedit;
    end;
end;

procedure OpenPack(OpenItem : Cardinal);
begin
    if dead and not Connected then exit;
    if (LastContainer <> OpenItem) and (dead = false) then
    begin
      repeat
        if not Connected then exit;
        CheckLag(5000);
        Checksave;
        UseObject(OpenItem);
        wait(1000);
      until LastContainer = OpenItem;
    end;
end;

begin
    if not GetARStatus then SetARStatus(True);
    FindDistance := 50;
    repeat
        OpenPack(Backpack);
        CheckLag(60000);
        Proces;
    until Dead;
    //AddToSystemJournal(Err);
    if GetARStatus then SetARStatus(False);
    Disconnect;
end.