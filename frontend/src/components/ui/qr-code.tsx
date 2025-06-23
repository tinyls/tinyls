import { QRCodeCanvas } from "qrcode.react";
import { Copy, Download } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { useRef } from "react";

interface QrCodeProps {
  value: string;
  size?: number;
}

function downloadStringAsFile(data: string, filename: string) {
  let a = document.createElement("a");
  a.download = filename;
  a.href = data;
  a.click();
}

export function QRCode({ value, size = 200 }: QrCodeProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const onCopyClick = async () => {
    const node = canvasRef.current;
    if (node == null) {
      return;
    }
    try {
      const blob = await new Promise<Blob | null>((resolve) =>
        node.toBlob(resolve, "image/png")
      );
      if (!blob) {
        throw new Error("Failed to create image blob.");
      }
      navigator.clipboard.write([new ClipboardItem({ [blob.type]: blob })]);
      toast.success("Copied to clipboard");
    } catch (error) {
      console.error(error);
      toast.error("There was a problem with copying.");
    }
  };

  const onCanvasDownloadClick = () => {
    const node = canvasRef.current;
    if (node == null) {
      return;
    }
    const dataURI = node.toDataURL("image/png");
    downloadStringAsFile(dataURI, "qrcode.png");
  };

  return (
    <>
      <div className="relative">
        <QRCodeCanvas
          ref={canvasRef}
          value={value}
          size={size}
          bgColor={"#ffffff"}
          fgColor={"#000000"}
          level={"L"}
        />
      </div>
      <div className="flex space-x-2">
        <Button variant="outline" size="sm" onClick={onCopyClick}>
          <Copy className="mr-2 h-4 w-4" />
          Copy
        </Button>
        <Button size="sm" onClick={onCanvasDownloadClick}>
          <Download className="mr-2 h-4 w-4" />
          Download
        </Button>
      </div>
    </>
  );
}
