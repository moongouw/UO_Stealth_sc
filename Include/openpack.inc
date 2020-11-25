procedure OpenPack(OpenItem : Cardinal);
var
t : integer;
begin
    if dead and not Connected then exit;
    if (LastContainer <> OpenItem) and (dead = false) then
    begin 
        t := 0;
        repeat
            t := t + 1;
            if not Connected then exit;
            CheckLag(5000);
            Checksave;
            UseObject(OpenItem);
            wait(1000);
        until (LastContainer = OpenItem) or (t >= 15);
        if t >= 15 then AddToSystemJournal('openpack.inc: не могу открыть контейнер! с ним что то не так!'); 
    end;
end;