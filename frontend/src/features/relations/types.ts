export type RelationType = "SUPPORTS" | "TENSION" | "REDUNDANT" | "OVERLOAD";

export type GoalRef = {
  templateCode: string | null;
  directionCode: string | null;
  period: string | null;
};

export type RelationRequest = { goals: GoalRef[]; candidate: GoalRef | null };

export type RelationHint = {
  type: RelationType;
  involved: string[];
  suggestion: string | null;
  reason: string;
};
