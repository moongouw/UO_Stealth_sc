Program Statistics;
var
    X0,Y0,Z0,Tile: Word;
    TileStatics: TStaticCell;
    C, W0: Byte;
begin

      // X0:=GetX(Self)+2;
      // Y0:=GetY(Self);
      // Z0:=GetZ(Self);
      W0:=WorldNum;

     ClientPrintEx(0, 70, 0, 'Кликни на тайл');

      ClientRequestObjectTarget;
      repeat
        if ClientTargetResponsePresent then begin
          X0:=ClientTargetResponse.X;
          Y0:=ClientTargetResponse.Y;
          Z0:=ClientTargetResponse.Z;
        end;
        wait(500);
      until X0 > 0;

      TileStatics:=ReadStaticsXY(X0,Y0,W0);     
      C:=GetLayerCount(X0,Y0,WorldNum);
      Addtosystemjournal('------------------------------------------------');
      Addtosystemjournal('GetLayerCount = '+IntToStr(c)+' | X = ' +IntToStr(X0)+' | Y = ' +IntToStr(Y0)+' | Z = '+IntToStr(Z0)+' | World = ' +IntToStr(W0));
      If C > 0 then begin
      Addtosystemjournal('Layers = '+IntToStr(TileStatics.StaticCount)+' | Tile = ' +IntToStr(TileStatics.Statics[0].Tile)+' | X = ' +IntToStr(TileStatics.Statics[0].X)+' | Y = ' +IntToStr(TileStatics.Statics[0].Y)+' | Z = ' +IntToStr(TileStatics.Statics[0].Z)+' | Color = ' +IntToStr(TileStatics.Statics[0].Color));
      end else begin
      Addtosystemjournal('Тайл не имеет слоёв (как чёрные тайлы в пещере для мининга), поэтому массив Statics[0] пуст.');
      Addtosystemjournal('и данных "TileStatics.StaticCount", "TileStatics.Statics[0].Tile", "TileStatics.Statics[0].X", "TileStatics.Statics[0].Y)", "TileStatics.Statics[0].Z", "TileStatics.Statics[0].Color" - не существует.');
      end;
      Addtosystemjournal('Tile = ' +IntToStr(GetMapCell(X0,Y0,WorldNum).Tile)+' | Z = ' +IntToStr(GetMapCell(X0,Y0,WorldNum).Z));
      Tile:= (GetMapCell(X0,Y0,WorldNum).Tile);
      Addtosystemjournal('GetTileFlags: ' + IntToStr(GetTileFlags(8,Tile)));
      Addtosystemjournal('Flags = '+IntToHex(GetLandTileData(Tile).Flags,8)+' | TextureID = ' +IntToStr(GetLandTileData(Tile).TextureID));
      Addtosystemjournal('Flags = '+IntToHex(GetStaticTileData(Tile).Flags,8)+' | Weight = ' +IntToStr(GetStaticTileData(Tile).Weight)+' | Height = ' +IntToStr(GetStaticTileData(Tile).Height));

end.