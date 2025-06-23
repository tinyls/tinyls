import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";

const SearchBar = ({
  value,
  onChange,
}: {
  value: string;
  onChange: (v: string) => void;
}) => {
  return (
    <div className="relative flex-1">
      <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
      <Input
        type="search"
        placeholder="Search URLs..."
        className="pl-8"
        value={value}
        onChange={(e) => onChange(e.target.value)}
      />
    </div>
  );
};

export default SearchBar;
