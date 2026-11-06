Add-Type -AssemblyName System.Drawing

$output = Join-Path $PSScriptRoot "src\main\resources\images\cat-player.png"
$bitmap = New-Object System.Drawing.Bitmap 360, 360
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)
$graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
$graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
$graphics.Clear([System.Drawing.Color]::Transparent)

$fur = New-Object System.Drawing.SolidBrush ([System.Drawing.ColorTranslator]::FromHtml("#607989"))
$furLight = New-Object System.Drawing.SolidBrush ([System.Drawing.ColorTranslator]::FromHtml("#7892A2"))
$dark = New-Object System.Drawing.SolidBrush ([System.Drawing.ColorTranslator]::FromHtml("#2E3E48"))
$white = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::White)
$cream = New-Object System.Drawing.SolidBrush ([System.Drawing.ColorTranslator]::FromHtml("#F4E4BF"))
$pink = New-Object System.Drawing.SolidBrush ([System.Drawing.ColorTranslator]::FromHtml("#D77A86"))
$red = New-Object System.Drawing.SolidBrush ([System.Drawing.ColorTranslator]::FromHtml("#B93E3D"))
$shadow = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(45, 40, 40, 40))
$outline = New-Object System.Drawing.Pen ([System.Drawing.ColorTranslator]::FromHtml("#273740")), 5
$outline.Alignment = [System.Drawing.Drawing2D.PenAlignment]::Center
$line = New-Object System.Drawing.Pen ([System.Drawing.ColorTranslator]::FromHtml("#273740")), 3
$line.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
$line.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
$tailPen = New-Object System.Drawing.Pen ([System.Drawing.ColorTranslator]::FromHtml("#526C7C")), 20
$tailPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
$tailPen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round

$graphics.FillEllipse($shadow, 88, 300, 190, 24)

$tail = New-Object System.Drawing.Drawing2D.GraphicsPath
$tail.AddBezier(126, 255, 80, 260, 48, 236, 58, 188)
$graphics.DrawPath($tailPen, $tail)

$graphics.FillEllipse($fur, 118, 205, 126, 108)
$graphics.DrawEllipse($outline, 118, 205, 126, 108)
$graphics.FillEllipse($cream, 145, 225, 72, 76)

$graphics.FillEllipse($fur, 105, 284, 58, 28)
$graphics.FillEllipse($fur, 202, 284, 58, 28)
$graphics.DrawEllipse($outline, 105, 284, 58, 28)
$graphics.DrawEllipse($outline, 202, 284, 58, 28)

$leftEar = New-Object System.Drawing.Point[] 3
$leftEar[0] = New-Object System.Drawing.Point 107, 103
$leftEar[1] = New-Object System.Drawing.Point 127, 22
$leftEar[2] = New-Object System.Drawing.Point 181, 85
$rightEar = New-Object System.Drawing.Point[] 3
$rightEar[0] = New-Object System.Drawing.Point 253, 103
$rightEar[1] = New-Object System.Drawing.Point 233, 22
$rightEar[2] = New-Object System.Drawing.Point 179, 85
$graphics.FillPolygon($fur, $leftEar)
$graphics.FillPolygon($fur, $rightEar)
$graphics.DrawPolygon($outline, $leftEar)
$graphics.DrawPolygon($outline, $rightEar)

$leftInner = New-Object System.Drawing.Point[] 3
$leftInner[0] = New-Object System.Drawing.Point 123, 83
$leftInner[1] = New-Object System.Drawing.Point 132, 43
$leftInner[2] = New-Object System.Drawing.Point 158, 79
$rightInner = New-Object System.Drawing.Point[] 3
$rightInner[0] = New-Object System.Drawing.Point 237, 83
$rightInner[1] = New-Object System.Drawing.Point 228, 43
$rightInner[2] = New-Object System.Drawing.Point 202, 79
$graphics.FillPolygon($pink, $leftInner)
$graphics.FillPolygon($pink, $rightInner)

$graphics.FillEllipse($furLight, 89, 68, 182, 171)
$graphics.DrawEllipse($outline, 89, 68, 182, 171)

$graphics.FillEllipse($white, 122, 104, 62, 70)
$graphics.FillEllipse($white, 178, 104, 62, 70)
$graphics.DrawEllipse($line, 122, 104, 62, 70)
$graphics.DrawEllipse($line, 178, 104, 62, 70)
$graphics.FillEllipse($cream, 134, 116, 38, 44)
$graphics.FillEllipse($cream, 190, 116, 38, 44)
$graphics.FillEllipse($dark, 146, 126, 17, 27)
$graphics.FillEllipse($dark, 198, 126, 17, 27)
$graphics.FillEllipse($white, 149, 130, 6, 8)
$graphics.FillEllipse($white, 201, 130, 6, 8)

$graphics.FillEllipse($white, 140, 174, 43, 38)
$graphics.FillEllipse($white, 178, 174, 43, 38)
$graphics.DrawEllipse($line, 140, 174, 43, 38)
$graphics.DrawEllipse($line, 178, 174, 43, 38)
$graphics.FillEllipse($dark, 172, 179, 17, 13)

$graphics.DrawArc($line, 169, 185, 24, 23, 8, 164)
$graphics.DrawArc($line, 171, 202, 20, 13, 12, 156)
$graphics.DrawLine($line, 138, 187, 91, 177)
$graphics.DrawLine($line, 138, 199, 88, 198)
$graphics.DrawLine($line, 139, 211, 94, 220)
$graphics.DrawLine($line, 221, 187, 268, 177)
$graphics.DrawLine($line, 221, 199, 271, 198)
$graphics.DrawLine($line, 220, 211, 265, 220)

$leftBow = New-Object System.Drawing.Point[] 3
$leftBow[0] = New-Object System.Drawing.Point 180, 238
$leftBow[1] = New-Object System.Drawing.Point 140, 224
$leftBow[2] = New-Object System.Drawing.Point 145, 258
$rightBow = New-Object System.Drawing.Point[] 3
$rightBow[0] = New-Object System.Drawing.Point 180, 238
$rightBow[1] = New-Object System.Drawing.Point 220, 224
$rightBow[2] = New-Object System.Drawing.Point 215, 258
$graphics.FillPolygon($red, $leftBow)
$graphics.FillPolygon($red, $rightBow)
$graphics.DrawPolygon($outline, $leftBow)
$graphics.DrawPolygon($outline, $rightBow)
$graphics.FillEllipse($red, 170, 230, 20, 18)
$graphics.DrawEllipse($line, 170, 230, 20, 18)

$bitmap.Save($output, [System.Drawing.Imaging.ImageFormat]::Png)

$tail.Dispose()
$tailPen.Dispose()
$line.Dispose()
$outline.Dispose()
$shadow.Dispose()
$red.Dispose()
$pink.Dispose()
$cream.Dispose()
$white.Dispose()
$dark.Dispose()
$furLight.Dispose()
$fur.Dispose()
$graphics.Dispose()
$bitmap.Dispose()

Write-Output $output
